
import * from std;
import * from processing;

// brian's brain CA
//
// inspired by clojure version (and followups) at
// http://www.bestinclass.dk/index.php/2009/10/brians-functional-brain/

//
// constants
//

PI = 3.1415927;
TWO_PI = 6.2831855;
HALF_PI = 1.5707964;

(W, H) = (1024, 1024);                            // screen width, height
(Wf, Hf) = (i2f(W), i2f(H));                    // convenience (loosely speaking)
(MAJRAD, MINRAD) = (0.25 *. Wf, 0.125 *. Wf);   // major and minor torus radii

FRICTION = 0.95;                                // for interactive torus rotation

(INITCOLS, INITROWS) = (256, 256);              // initial CA dimensions

(ON, DYING, OFF) = (2, 1, 0);                   // cell states

MSNANOS = 1000000.0;
SECNANOS = 1000000000.0;

//
// state
//

// CA state
dims = box(0, 0);                               // dimensions of grid/torus
cells:*[Int] = box(take(0, [0]));               // cells are kept in a linear list
check:*[Int] = box(take(0, [0]));               // subset of cell indexes to check at each step
regions:*[[Int]] = box(take(0, [[0]]));         // for each cell, a list of neighbor indexes
tormesh:*[[(Double, Double, Double)]] = box(take(0, [[(0.0, 0.0, 0.0)]]));   // 3d surface mesh for torus, list of point lists

// settings
bgproc = box(true);                             // background processing on/off
njobs = box(3);                                 // number of parallel jobs - init to avail cpus
draw3d = box(false);                            // draw torus/flat grid
alpha = box(false);                             // draw solid or transparent torus
paused = box(false);                            // pause CA evolution on/off

// view state
rot = box(0.0, 0.0, 0.0);                       // torus rotation
rotv = box(0.0, 0.0, 0.0);                      // torus rotational velocity

// time tracking
lastdraw = box(0L);
drawfps = box(0.0);
stepfps = box(0.0);
num_fps_samples = box(0);

update_stepfps(newfps) {
    stepfps <- { f =>
        n = postinc(num_fps_samples);
        ((f *. i2f(n)) + newfps) /. i2f(n + 1)
    }
};

reset_stepfps() { puts((stepfps, num_fps_samples), (0.0, 0)) };

//
// functions
//

// initialize state over given dimensions -
// state includes CA cells and precalculated data
reset(cols, rows) {

    // generate a new list of randomized cells
    seedcells(n) { repeat(n, {iif(rand(100) < 50, ON, OFF)}) };

    // helper - convert between cell (x, y) position and cell index
    xy2i(x, y) { y * cols + x };
    i2xy(i) { (i % cols, i / cols) };

    // helper - wrap (x, y) into ([0, cols), [0, rows))
    wrap(x, y) { ((x + cols) % cols, (y + rows) % rows) };

    // helper - precalculate region index for each position in the new state
    calcregions() {
    
        // the region around index i is the perimeter of cells
        // around i's (x,y) position. don't include i.
        region(i) {

            // translate index i into an (x,y) position
            (x, y) = i2xy(i);

            // generate the 3x3 square of (x,y) positions centered on i,
            // then convert them to indexes
            square = cross(range(x - 1, 3), range(y - 1, 3)) | { xy2i(wrap($0, $1)) };

            // remove i itself and return the list
            filter(square, { $0 != i })
        };

        // return a list of regions, one for each index in the new state
        count(rows * cols) | region
    };

    // helper - calculate torus mesh. schooled by
    // http://www.davebollinger.com/works/p5/splineextrude/SplineExtrude.pde.txt
    calcmesh() {

        // calculate mesh for one row of new state
        calcrow(r) {

            // intersection point of angle t with torus major radius circle
            torpt(t) { (MAJRAD *. cos(t), MAJRAD *. sin(t)) };

            // get ring left, mid, right intersection points
            pts = [i2f(r), i2f(r) + 0.5, i2f(r + 1)] | { torpt($0 /. i2f(rows) *. TWO_PI) };
            (p0, p1, p2) = (pts[0], pts[1], pts[2]);

            // chord across ring at MAJRAD
            (dx, dy) = (p2.0 -. p0.0, p2.1 -. p0.1);

            // set up a matrix to push ring points through - torus will ultimately lay flat
            // on xy plane; this flips it onto its side and makes origin mid-ring on the torus
            // major radius circle, so we can generate ring from a simple xy circle.
            mat = prpmatrix3d();
            prmattranslate(mat, p1.0, p1.1, 0.0);
            prmatrotatez(mat, atan2(dy, dx));
            prmatrotatey(mat, HALF_PI);

            // calculate the location of (r, c) on torus surface
            calccol(c) {
                t = i2f(c) /. i2f(cols) *. TWO_PI;
                prmatmult(mat, MINRAD *. cos(t), MINRAD *. sin(t), 0.0)
            };

            // return list of locations for each col in this row
            count(cols) | calccol
        };

        // return list of precalculated mesh rows - parallelize per njobs global
        //pmapn(count(rows), calcrow, get(njobs))
        count(rows) | calcrow
    };

    // generate new state locally
    n = cols * rows;
    newdims = (cols, rows);
    newcells = seedcells(n);
    newcheck = count(n);
    newmask = rep(n, true);
    newregions = calcregions();
    newmesh = calcmesh();

    // swap new state into program globals
    puts((dims, cells, check, regions, tormesh),
        (newdims, newcells, newcheck, newregions, newmesh));

    reset_stepfps();
};

//
// given CA state of
// - list of cells, each ON, DYING, or OFF
// - list of cell neighborhoods (lists of adjacent positions)
// - list of indexes of cells to check (those that changed at last step)
// evolve the CA one step, and return new cells and checklist.
//
// cell evolution rule:
// ON cells start DYING
// DYING cells turn OFF
// OFF cells with 2 active neighbors turn ON
//
compute(cells:[Int], check:[Int], regions:[[Int]])
{
    // return number of ON neighbors of the cell at index i
    nabes(i) { size(filter(regions[i], { cells[$0] == ON })) };

    // number of jobs to split filtering into
    nj = *njobs;

    // filter predicates
    preds = [
        { i => cells[i] == ON },
        { i => cells[i] == DYING },
        { i => cells[i] == OFF && { nabes(i) == 2 } }
    ];

    // collect indexes of changing cells (parallel filters)
    [dying, off, on] = preds | { pfiltern(check, $0, nj) };

    // given a list of cells, indexes and a state, return
    // a new version with cells at indexes set to state
    // TODO decomp params!!
    setcells(cells:[Int], args:([Int], Int)) {
        (ixs, state) = args;
        listsets(cells, ixs, [state])
    };

    // update cells as specified by CA rules
    newcells = evolve(cells, setcells, [(dying, DYING), (off, OFF), (on, ON)]);

    // new checklist is all changed cells, and neighbors of newly ON cells
    newcheck = unique(flatten([off, dying, on] + mapll(on, regions)));

    // return new cells and checklist
    (newcells, newcheck)
};


//
// compute the next step of our global CA state
//
step()
{
    // time tracking
    t0 = nanotime();

    // evolve and update CA state in a transaction
    transfers((cells, check), compute, (cells, check, regions));

    t1 = nanotime();
    update_stepfps(SECNANOS /. l2f(lminus(t1, t0)));

    // when stepping in our own task, wait until/unless paused is off
    when(*bgproc, { await(paused, not) })
};

// ------------

//
// view
//

// cell fill colors, keyed by state
FILL = [(0x19, 0x3c, 0x4c, 0xff), (0x00, 0x7f, 0xba, 0xff), (0xff, 0xff, 0xff, 0xff)];
TRANSBG = (48, 48, 48, 48);

// fills keyed by alpha (transparent torus) flag
FILLS = [false: FILL, true: [TRANSBG] + drop(1, FILL)];

AXISLEN = 50.0;

// draw CA as a torus - not optimized, draws the whole thing every time,
// even if it's not moving
drawtor() {
    // get synchronized snapshot of dims, cells, torus mesh
    ((cols, rows), cur, mesh) = gets(dims, cells, tormesh);

    // helper - draw a little x/y/z thing in the middle of the donut hole
    drawaxes() {
        prstrokeweight(2);
        prstroke(255,0,0);
        prline3d(-.AXISLEN /. 2.0, 0.0, 0.0, AXISLEN, 0.0, 0.0);
        prstroke(0,255,0);
        prline3d(0.0, -.AXISLEN /. 2.0, 0.0, 0.0, AXISLEN, 0.0);
        prstroke(0,0,255);
        prline3d(0.0, 0.0, -.AXISLEN /. 2.0, 0.0, 0.0, AXISLEN);
    };

    // helper - translate x,y position to index, using current cols
    xy2i(x, y) { y * cols + x };

    // fill colors by cell state
    fill = FILLS[*alpha];

    // helper - draw a row of cells onto the torus
    drawrow(r) {
        drawcell(c) {
            cell = cur[xy2i(c % cols, r)];
            prfillrgba(fill[cell]);
            m0 = mesh[r][c % cols];
            m1 = mesh[(r + 1) % rows][c % cols];
            prvertex3d(m0.0, m0.1, m0.2);
            prvertex3d(m1.0, m1.1, m1.2);
        };
        prbeginshapemode(#QUAD_STRIP);
        for(count(cols + 1), drawcell);
        prendshape()
    };

    // draw info
    drawinfo();
    // light and rotate the space
    prlights();
    prdirectionallight(128.0, 192.0, 192.0, 1.0, 0.0, 0.0);
    prtranslate(Wf /. 2.0, Hf /. 2.0);
    r = *rot;
    prrotatex(r.0);
    prrotatey(r.1);
    prrotatez(r.2);
    // draw axis indicator
    drawaxes();
    // draw torus
    prnostroke();
    for(count(rows), drawrow)
};

// draw CA as a simple grid covering the entire screen
drawgrid()
{
    // get synchronized snapshow of dims, cells
    ((cols, rows), cur) = gets(dims, cells);

    // scale factors
    (scalex, scaley) = (Wf /. i2f(cols), Hf /. i2f(rows));

    // helper - convert index to position, using current cols
    i2xy(i) { (i % cols, i / cols) };

    // draw cell at index i onto screen
    drawcell(i) {
        c = cur[i];
        (x, y) = i2xy(i);
        prfillrgba(FILL[c]);
        prrect(i2f(x) *. scalex, i2f(y) *. scaley, scalex, scaley)
    };

    // draw grid
    prnostroke();
    for(count(cols * rows), drawcell);

    // overlay heads-up info
    drawinfo();
    ()
};

// helper - format a float for diagnostic purposes
ffmt(f, places) {
    s = f2s(f);
    p = strfind(s, ".");
    if(strfind(s, "E") < strlen(s), {
        iif(f <. 0.0, "-E", "E")
    }, {
        if(endswith(s, ".0"), {
            strdrop(-2, s)
        }, {
            strtake(min(strlen(s), p + places + sign(places)), s)
        })
    })
};

// draw info overlay
drawinfo() {
    s = "grid: " + tostr(*dims) +
        "  tasks: " + tostr(iif(*bgproc, 1, 0) + *njobs) +
        //"  calc jobs: " + tostr(*njobs) +
        "  draws/sec: " + ffmt(*drawfps, 1) +
        "  calcs/sec: " + ffmt(*stepfps, 1);
    prfill(255);
    prtext(s, 5.0, 24.0);
};

drawview() {

    // time tracking
    now = nanotime();
    last = postput(lastdraw, now);
    drawelapsed = l2f(lminus(now, last));
    drawfps := SECNANOS /. drawelapsed;

    prbackground(0);

    if(!*draw3d, drawgrid, {
        // update rotation and rotational velocity
        rv = *rotv;

        when(rv.0 + rv.1 + rv.2 >. 0.0,
        {
            tocirc(r) { (r + TWO_PI) %. TWO_PI };

            el = drawelapsed /. MSNANOS;

            rot <- { (
                tocirc($0 + rv.0 *. el),
                tocirc($1 + rv.1 *. el),
                tocirc($2 + rv.2 *. el)
            ) };

            rotv <- { ($0 *. FRICTION, $1 *. FRICTION, $2 *. FRICTION) };
        });

        // draw
        drawtor()
    });

    // if no background task, update after drawing
    when(!*bgproc, step);
};

// ----------------------------

// torus movement

dragstate = box((#pos: (0, 0), #time: 0L));

grab() {
    dragstate := (#pos: prmouse(), #time: millitime());
    rotv := (0.0, 0.0, 0.0)
};

mouserot(pos:(Int, Int), pos0:(Int, Int)) {
    (x0, y0) = pos0;
    (x, y) = pos;
    (dx, dy) = (i2f(x - x0) /. Wf, i2f(y - y0) /. Hf);
    rho = fabs((Wf /. 2.0) -. i2f(x)) /. (Wf /. 2.0);
    ecc = fabs((Hf /. 2.0) -. i2f(y)) /. (Hf /. 2.0);
    rx = dy *. (1.0 -. rho) *. TWO_PI;
    ry = dx *. (1.0 -. ecc) *. TWO_PI;
    getsign(v, n) { iif(v < n / 2, -.1.0, 1.0) };
    (xsign, ysign) = ( getsign(x,W), getsign(y,H) );
    rz = (dy *. rho *. xsign *. PI) + (dx *. ecc *. -.ysign *. PI);
    (rx, ry, rz)
};

drag() {
    state = *dragstate;
    now = millitime();
    elapsed = l2f(lminus(now, state.time));
    when(elapsed >. 0.0, {
        pos = prmouse();
        (rx, ry, rz) = mouserot(pos, state.pos);
        dragstate := (#pos: pos, #time: now);
        rotv := (-.rx /. elapsed, ry /. elapsed, rz /. elapsed);
    });
};

// ----------------------------

// processing

// spawn a task to run step() continuously while global bgproc is true.
// The only difference between tasked and non-tasked execution models
// is whether step() gets called here, or inline in the draw function.
startbgtask() {
    spawn { while({*bgproc && prisopen}, step) }
};

// add a task
addtask() {
    reset_stepfps();
    if(*bgproc,
        { njobs <- inc },
        { bgproc <- not; startbgtask() })
};

// remove a task
removetask() {
    when(*bgproc, {
        reset_stepfps();
        if(*njobs > 1, { njobs <- dec }, { bgproc <- not })
    })
};

// scale dimensions by a given factor, reset CA.
// note: we do this in a separate task, so draw/step cycle continues
// on old cells until reset swaps in new state.
rescale(fact) {
    (c, r) = *dims;
    spawn { reset(round(i2f(c) *. fact), round(i2f(r) *. fact)) };
};

// open a processing window
// some handler functions given inline for brevity
propen("brian's brain - click to toggle 3D, drag to rotate, +/-: #tasks, </>: #cells, p: pause", [
    #setup: {
        prsizemode(W, H, #P3D);
        prtextfont(prloadfont("data/ArialMT-18.vlw"));
        reset(INITCOLS, INITROWS);
        when(*bgproc, startbgtask)     // start background task if needed
    },
    #draw: drawview,
    #mousePressed: { when(prmousebutton() == #LEFT, grab) },
    #mouseDragged: { when(prmousebutton() == #LEFT, drag) },
    #mouseClicked: { when(prmousebutton() == #LEFT, { draw3d <- not }) },
    #keyTyped: {
        switch(strfind("+-ap<>", prkey()), [
            0: addtask,
            1: removetask,
            2: { alpha <- not },            // toggle alpha (transparent torus)
            3: { paused <- not },           // toggle pause
            4: { rescale(0.75) },           // smaller grid
            5: { rescale(4.0 /. 3.0) },     // bigger grid
            6: { () }
        ])
    }
]);

