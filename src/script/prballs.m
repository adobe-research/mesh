
import processing;

//
// bouncy balls
//

// --------------------------

// 2d vector api 

// type V2 is a synonym for (Double, Double) - any pair of floats is a V2
type V2 = (Double, Double);

VZERO = (0.0, 0.0);                                         // zero vector

v2(x, y) { (i2f(x), i2f(y)) };                              // V2 from pair of ints

add(a, b) { (a.0 + b.0, a.1 + b.1) };                       // sum of two vectors
vsum(vs) { reduce(add, VZERO, vs) };                        // sum of a list of vectors
vavg(vs) { scale(vsum(vs), fdivz(1.0, i2f(size(vs)))) };    // average of a list of vectors
diff(a, b) { (a.0 -. b.0, a.1 -. b.1) };                    // difference of two vectors
scale(v, f) { (v.0 *. f, v.1 *. f) };                       // scale a vector by a magnitude
mag(x, y) { sqrt(mag2(x, y)) };                             // vector magnitude
mag2(x, y) { fsq(x) + fsq(y) };                             // square of vector magnitude
dist(a, b) { mag(diff(a, b)) };                             // distance (vectors are x,y positions)
dot(a, b) { a.0 *. b.0 + a.1 *. b.1 };                      // dot product

// rotate a vector (offset from origin) through the angle represented by sin/cos
rot(x, y, sin, cos) { (cos *. x + sin *. y, cos *. y -. sin *. x) };

// area of circle from radius
circarea(rad) { PI *. fsq(rad) };

// -------------------------

// constants

PI = 3.14159265;
MSNANOS = 1000000.0;
SECNANOS = 1000000000.0;

(W,H) = (1024, 1024);                                   // width and height of processing window

BOX = (x: 0.0, y: 0.0, w: i2f(W), h: i2f(H));       // convenience - play box as float rect
SMALLEXT = i2f(min(W,H));                               // convenience - smaller of width and height, as float

(MINR, MAXR) = (SMALLEXT /. 500.0, SMALLEXT /. 100.0);  // min and max radii, based on smaller of box dims
MAXV = SMALLEXT /. 750.0;                               // max initial velocity, based on smaller of box dims

CELLEXT = f2i(3.0 *. MAXR);                             // cells are squares 1.5 times max ball diameter on a side
CELLCOLS = W / CELLEXT + sign(W % CELLEXT);             // number of cell columns
CELLROWS = H / CELLEXT + sign(H % CELLEXT);             // number of cell rows
CELLIXS = cross(count(CELLCOLS), count(CELLROWS));      // precalculated list of cell index (x, y) pairs

// neighborhood matrix: for each cell at a given (x, y),
// NABE[y][x] is a list oc (x, y) coordinates of adjacent cells.
// e.g. NABE[1][1] = [(0, 0), (0, 1), (0, 2), ..., (2, 2)]
//
NABE = count(CELLROWS) | { r =>
    count(CELLCOLS) | { c =>
        adjx = fromto(max(0, c - 1), min(CELLCOLS - 1, c + 1));
        adjy = fromto(max(0, r - 1), min(CELLROWS - 1, r + 1));
        cross(adjx, adjy)
    }
};

WALLSQUEEZE = 0.1;                                      // wall squeeze force, per unit position
BALLSQUEEZE = 0.01 *. circarea(MINR);                   // ball squeeze force, based on min radius
WALLDEATH = 25;                                         // 1 in WALLDEATH chance that wall kills (slow) ball
ELASTICITY = 0.975;//0.935;                             // collision elasticity (pct energy retained on collision)
FRICTION = 0.0004;                                      // friction coefficient (pct velocity lost per milli)
DECAY = 0.0001;                                         // rate of aging (pct health lost per milli)
MORBID = 0.2;                                           // minimum health

BATCH = 500;

// state

// a Player is a record containing a unique id, position and velocity vectors,
// a radius, health (0.0 to 1.0) and a color.
type Player = (id:Int, pos:V2, vel:V2, rad:Double, health:Double, color:Int);

players : Box( [ Box(Player) ] ) = box([]);                                           // global list of boxed players
cells : [[ Box([ Box(Player) ]) ]] = repeat(CELLROWS, {repeat(CELLCOLS, {box([])})});     // grid of boxed player lists

// intrumentation
calctasks = box(availprocs() / 2);
turnfps = box(0.0);                                     // save turn() fps for display

// -------------------------

// Player

// id generator variable
idgen = box(0);

// helper - generate a random float, within a given range around a given float
jitter(f, r) { f + r *. (frand() -. 0.5)  };

//
// create a new player.
// id is taken from incremented global
// position is random but confined to a small box centered on passed value.
// velocity is random, between 0 and MAXV global in both axes.
// radius is random, between MINR and MAXR globals.
// color is random but bright :)
//
newborn(pos:V2)
{
    (id: postinc(idgen),
     pos: (jitter(pos.0, BOX.w *. 0.2), jitter(pos.1, BOX.h *. 0.2)),
     vel: (frand() *. MAXV -. MAXV /. 2.0, frand() *. MAXV -. MAXV /. 2.0),
     rad: MINR + frand() *. (MAXR -. MINR),
     health: 1.0,
     color: hsb2rgb(frand(), frand(), 1.0))
};

// util - mass of player is area of ball
mass(p:Player) { circarea(p.rad) };

// helper - update velocity
setvel(p:Player, vel)
{
    (id:p.id, pos:p.pos, vel:vel, rad:p.rad, health:p.health, color:p.color)
};

// helper - update velocity and health
setvelhealth(p:Player, vel, health)
{
    (id:p.id, pos:p.pos, vel:vel, rad:p.rad, health:health, color:p.color)
};

// helper - update position and health
setposhealth(p:Player, pos, health)
{
    (id:p.id, pos:pos, vel:p.vel, rad:p.rad, health:health, color:p.color)
};

// ---------

// cells

// cells are lists of boxed players - each cell holds all players in a particular
// square on-screen region. players are moved among cells as their position changes.

// cell coords from position
poscellxy(pos:V2)
{
    (min(W - 1, max(0, f2i(pos.0))) / CELLEXT, min(H - 1, max(0, f2i(pos.1))) / CELLEXT)
};

// cell from position
poscell(pos)
{
    (x,y) = poscellxy(pos);
    cells[y][x]
};

// cell from player's position
playercell(p:Player) { poscell(p.pos) };

// remove player from position-derived cell
removeplayer(pbox:*Player)
{
    playercell(*pbox) <- { remove($0, pbox) }
};

// add player to cell
addplayer(pbox:*Player, cell:*[*Player])
{
    cell <- { append($0, pbox) }
};

//
// transfer player from current position-derived cell to
// the given new cell. used by callers who then update
// the location of the player itself.
//
// note: in the current code, the caller already has a
// transaction open and owns this player box, but it's
// cheap to be future-proof here.
//
transferplayer(pbox:*Player, newpos)
{
    do
    {
        oldcell = playercell(*pbox);
        newcell = poscell(newpos);

        when(oldcell != newcell,
        {
            removeplayer(pbox);
            addplayer(pbox, newcell)
        })
    }
};

// ---------

// movement/collision

// reflect a velocity given position and boundaries (1-D)
reflect(lo, hi, pos, vel)
{
    if(pos <=. lo,
        { fabs(vel) *. ELASTICITY },
        { guard(pos <=. hi, vel, { -.fabs(vel) *. ELASTICITY }) })
};

// generate a squeeze force given position and (rubber-band) boundaries (1-D)
squeeze(lo, hi, pos)
{
    WALLSQUEEZE *. (lo -. fmin(lo, pos) + hi -. fmax(pos, hi))
};

//
// calculate new velocity from walls, player's position and velocity -
// near the boundary this is just the elastic bounce off box walls.
// if we're way outside, we add in a rubber-band squeeze
// proportional to how far ball is outside play box. Latter comes
// into play when ball has been dragged out
//
BAND_XGAP = BOX.w /. 50.0;
BAND_YGAP = BOX.h /. 50.0;
BAND = (x: BOX.x -. BAND_XGAP, y: BOX.y -. BAND_YGAP,
    w: BOX.w + 2.0 *. BAND_XGAP, h: BOX.h + 2.0 *. BAND_YGAP);

// reflect and/or squeeze when player interacts with wall.
// also, a wall can occasionally make a player very sick.
wallbounce(p:Player)
{
    rad = p.rad;
    ((px, py), (vx, vy)) = (p.pos, p.vel);
    (l, t, r, b) = (BOX.x + rad, BOX.y + rad, BOX.x + BOX.w -. rad, BOX.y + BOX.h -. rad);

    // reflect velocity against box walls when out of bounds
    rv = (reflect(l, r, px, vx), reflect(t, b, py, vy));

    // add squeeze force when way out of bounds
    (l2, t2, r2, b2) = (BAND.x + rad, BAND.y + rad, BAND.x + BAND.w -. rad, BAND.y + BAND.h -. rad);
    sq = scale((squeeze(l2, r2, px), squeeze(t2, b2, py)), 1.0 /. mass(p));

    // add a chance of sudden death, if ball is below a certain velocity
    newhealth = guard(rand(WALLDEATH) > 0 || {mag(vx, vy) >. 1.0}, p.health, {MORBID *. 0.5});

    (add(rv, sq), newhealth)
};

//
// calculate one-dimensional collision
//
collide1d(v0, m0, v1, m1)
{
    (v0retain, v0transfer) = (v0 *. (m0 -. m1), v0 *. m0 *. 2.0);
    (v1retain, v1transfer) = (v1 *. (m1 -. m0), v1 *. m1 *. 2.0);
    m = m0 + m1;
    ((v0retain + v1transfer) /. m, (v1retain + v0transfer) /. m)
};

//
// generate post-collision velocities from incoming pair of
// (position, velocity, mass).
//
// Basic technique (rotate system so collision happens on
// the x-axis, then use 1d conservation of momentum) from
// http://processing.org/learning/topics/circlecollision.html
//
collide(p0:V2, v0:V2, m0:Double, p1:V2, v1:V2, m1:Double)
{
    // get angle of relative position w.r.t. x axis
    relpos = diff(p0, p1);
    angle = atan2(relpos.1, relpos.0);
    (s, c) = (sin(angle), cos(angle));

    // rotate vectors so that collision happens on x-axis,
    // then calculate
    rot2x(v:V2) { rot(v.0, v.1, s, c) };
    (rv0, rv1) =  (rot2x(v0), rot2x(v1));
    newvs = collide1d(rv0.0, m0, rv1.0, m1);

    // return pair of post-collision velocities, rotated back out
    (rot(newvs.0, rv0.1, -.s, c), rot(newvs.1, rv1.1, -.s, c))
};

//
// bump a pair of boxed players
//
bump(pbox:*Player, qbox:*Player)
{
    (p, q) = (*pbox, *qbox);                // get players
    (pp, qp) = (p.pos, q.pos);              // locals for positions
    posdiff = diff(pp, qp);                 // position difference
    sepdist = p.rad + q.rad;                // separation distance

    // balls only interact when overlapping
    when(mag2(posdiff) -. fsq(sepdist) <=. 0.0, {
        (pv, qv) = (playervel(pbox), playervel(qbox));  // locals for velocities
        (pm, qm) = (mass(p), mass(q));                  // locals for masses
        veldiff = diff(pv, qv);                         // velocity difference

        // when moving toward each other, bounce and restore health.
        (newpv, newqv, newph, newqh) = if(dot(veldiff, posdiff) <=. 0.0, {
            (newpv, newqv) = collide(pp, pv, pm, qp, qv, qm);
            (scale(newpv, ELASTICITY), scale(newqv, ELASTICITY), 1.0, 1.0)
        }, {
            // otherwise, push balls apart with constant force.
            // (poor man's soft-body sim, spreads out crowds)
            (add(pv, scale(posdiff, BALLSQUEEZE /. pm)),
             add(qv, scale(posdiff, -.BALLSQUEEZE /. qm)),
             p.health, q.health)
        });

        when(!isdragging(pbox), { pbox := setvelhealth(p, newpv, newph) });
        when(!isdragging(qbox), { qbox := setvelhealth(q, newqv, newqh) });
    })
};

//
// calculate collisions between a boxed player
// and a list of boxed players
//
collisions(pbox:Box(Player), qboxes:[Box(Player)])
{
    p = *pbox;

    // first, apply any wall forces when not dragging
    when(!isdragging(pbox), {
        (wbounce, whealth) = wallbounce(p);
        when(wbounce != p.vel, {
            pbox <- { setvelhealth($0, wbounce, whealth) }
        })
    });

    // bump with everyone after us in creation order -
    // ensures each pair is bumped at most once
    after = filter(qboxes, { qb:*Player => (*qb).id > p.id });
    for(after, { bump(pbox, $0) });
};

//
// calculate collisions for players in cell (cx, cy)
//
cellcollisions(cx, cy)
{
    cellplayers = *(cells[cy][cx]);

    // collect players to test against - this cell and adjacent cells
    testplayers = flatten(NABE[cy][cx] | { *(cells[$1][$0]) });

    // test each player in our cell against players
    // in our neighborhood
    for(cellplayers, { collisions($0, testplayers) });
};

// ----------

// state evolution

//
// update a player to reflect the passage of a given number of millis.
// also, update player's home cell based on current position.
// players are not subject to contention during aging, so no need
// to wrap this update in a transaction.
//
age(pbox:*Player, millis:Double)
{
    // snapshot player
    p = *pbox;

    // new position from current velocity and elapsed time
    newpos = add(p.pos, scale(p.vel, millis));

    // transfer player to cell of new position
    transferplayer(pbox, newpos);

    // scrub off some health
    newhealth = p.health *. (1.0 -. millis *. DECAY);

    // scrub off some velocity
    newvel = scale(p.vel, 1.0 -. millis *. FRICTION);

    // shrink with health, wink out of existence at threshold
    newrad = p.rad *. fmin(1.0, fmax(0.0, newhealth  -. MORBID) *. 300.0);

    // new player record
    newp = (
        id: p.id,
        pos: newpos,
        vel: newvel,
        rad: newrad,
        health: newhealth,
        color: p.color
    );

    // update player
    pbox := newp;
};

//
// update step: update player states and calculate collisions
// given time elapsed since last turn.
// in single-tasked mode, this is called synchronously
// from the draw function, otherwise in its own task.
// additional tasks may be used to parallelize against
// the grid of cells.
//
turn(last)
{
    // do some time-related bookkeeping
    start = nanotime();
    elapsed = l2f(lminus(start, last));
    turnfps := SECNANOS /. elapsed;

    // used in a few places
    ct = *calctasks;

    // calculate collisions by cell, either serially or in multiple parallel jobs
    if(ct <= 1,
        { for(CELLIXS, cellcollisions) },
        { pforn(CELLIXS, cellcollisions, ct) });

    // update players to reflect the passage of time (limited)
    elapsedms = fmin(elapsed /. MSNANOS, 100.0);
    if (ct <= 1,
        { for(*players, { age($0, elapsedms) }) },
        { pforn(*players, { age($0, elapsedms) }, ct) });

    // remove dead players
    dead = filter(*players, { pb:*Player => (*pb).health <=. MORBID });
    when(!empty(dead), { removeplayers(dead) });

    // return our start time
    start
};

// add players to list, and to their respective
// position-derived cells
//
addplayers(pboxes:[*Player])
{
    players <- { $0 + pboxes };
    for(pboxes, { addplayer($0, playercell(*$0)) })
};

// remove players from list, and from their respective
// position-derived cells
//
removeplayers(pboxes:[*Player])
{
    players <- { difference($0, pboxes) };
    for(pboxes, removeplayer)
};

// create a new batch of n players at a given position
//
newbatch(pos, n)
{
    addplayers(repeat(n, { box(newborn(pos)) }));
    ()
};

// spawn a task that calls turn() until done multitasked
// playing. broken out so that we can toggle single-tasked
// execution
//
startturntask()
{
    spawn {
        cycle(nanotime(), {_ => *calctasks > 0 && prisopen}, turn)
    }
};

// create a batch of players, loop on turn() while playing
startplay()
{
    newbatch(v2(W/2, H/2), BATCH);
    startturntask()
};

// ---------

// drag/throw
// TODO encapsulated version

// sliding window of (mouse position, sample time)
HISTMAX = 50;

type DragHist = [(pos:V2, time:Long)];
draghist:*DragHist = box([(pos: VZERO, time: 0L)]);
dragstartpos = box(VZERO);

// map from players currently being dragged, to their offsets
// from initial drag position
type DragInfo = [*Player : V2];
draginfo:*DragInfo = box([:]);

// apply function to dragged players
fordraglist(f) { for(keys(*draginfo), f) };

// true if the player is currently being dragged
isdragging(p:*Player) { iskey(*draginfo, p) };

// offset of player center from drag position
dragoffset(p:*Player) { guard(!isdragging(p), VZERO, {(*draginfo)[p]}) };

// player velocity - uses drag velocity if the player is being dragged
playervel(pbox:*Player) { if(isdragging(pbox), dragvel, {(*pbox).vel}) };

// get current drag state
dragstate() { last(*draghist) };

// get drag velocity, averaged over history
dragvel()
{
    hist = *draghist;
    (s0, s1) = (head(hist), last(hist));
    dist = diff(s1.pos, s0.pos);
    elap = lminus(s1.time, s0.time);
    scale(dist, fdivz(1000000.0, l2f(elap)));
};

// get total drag distance
dragdist() { diff(dragstate().pos, *dragstartpos) };

// initialize drag history, return current state
initdrag()
{
    pos = v2(prmouse());
    draghist := [(pos: pos, time: nanotime())];
    dragstartpos := pos;
    draginfo := hittest(pos)
};

// update drag history, return current state
updatedraghist()
{
    state = (pos: v2(prmouse()), time: nanotime());
    draghist <- { hist => append(take(-min(size(hist), HISTMAX - 1), hist), state) };
    state
};

// grab players at x,y for dragging, initialize drag-related globals,
// update dragged players to zero velocity, full health
grab()
{
    initdrag();
    fordraglist({ $0 <- { setvelhealth($0, VZERO, 1.0) } })
};

// find players at a given point, return map of drag info structs to offsets
hittest(pt:V2)
{
    checkplayer(hits:DragInfo, pbox:*Player) {
        p = *pbox;
        offset = diff(p.pos, pt);
        guard(mag(offset) >. p.rad, hits, { mapset(hits, pbox, offset) })
    };

    evolve([:], checkplayer, *players)
};

// move dragged players to new x, y
drag()
{
    state = updatedraghist();
    dragpos = state.pos;

    dragplayer(pbox:*Player) {
        do {
            newpos = add(dragpos, dragoffset(pbox));
            transferplayer(pbox, newpos);
            pbox <- { setposhealth($0, newpos, 1.0) }
        }
    };

    // move all players in drag map
    fordraglist(dragplayer)
};

// handle end of drag - throw dragged players
release()
{
    vel = dragvel();
    fordraglist({ $0 <- { setvel($0, vel) } });
    draginfo := [:];
};

// -----------------

// drawing

// local mode
DIAGS = [#none,#vel,#cells,#health];        // diagnostic states
xray = box(#none);                          // current diagnostic mode

// local state
lastdraw = box(0L);                         // nanotime of last render, used to calc FPS
lastrenderturn = box(0L);                   // nanotime of last in-task turn, need when running single-tasked

// convenience - drives some display decisions
diagmode() { *xray != #none };

// helper - unpack an rgb int into a triple
unpack(rgb) { (rgb / 0x10000 % 0x100, rgb / 0x100 % 0x100, rgb % 0x100) };

// draw a player ball
drawplayer(p:Player)
{
    (r,g,b) = unpack(p.color);
    prfillrgba(r, g, b, 31 + f2i(p.health *. 224.0));         // health determines alpha
    prellipse(p.pos.0, p.pos.1, p.rad *. 2.0, p.rad *. 2.0)
};

// draw info overlay
drawhud(renderfps)
{
    prtextfont(diagfont());

    s = "balls: " + i2s(size(*players)) +
        "  tasks:" + i2s(1 + *calctasks) +
        "  draws/sec: " + ffmt(renderfps, 0) +
        "  turns/sec: " + ffmt(*turnfps, 0);

    prfillrgba(0, 0, 0, 128);
    prrect(0.0, 0.0, prtextwidth(s) + 10.0, 22.0);
    prfill(255);
    prtext(s, 5.0, 14.0);
};

// draw players and track time. display additional info if in diag mode
drawplayers()
{
    // do some time-related recordkeeping
    now = nanotime();
    last = getput(lastdraw, now);
    renderfps = SECNANOS /. l2f(lminus(now, last));

    // draw underlays, then players, then overlays
    prbackground(0);
    when(diagmode(), drawcells);
    for(*players, { drawplayer(*$0) });
    when(diagmode(), {for(*players, drawinfo)});

    // when single-tasked, do game turn in rendering task
    when(*calctasks == 0,
    {
        last = *lastrenderturn;
        next = turn(last);
        lastrenderturn := next
    });

    // draw heads-up info
    drawhud(renderfps);
};

// roll through diagnostic modes
nextdiag()
{
    xray <- { diag => DIAGS[(find(DIAGS, diag) + 1) % size(DIAGS)] };
};

// draw the checkerboard of cells
drawcells()
{
    // helper - draw a single cell, in color derived from grid position
    drawcell(x,y)
    {
        (r,g,b) = unpack(hsb2rgb(i2f(y + x) /. i2f(CELLCOLS + CELLROWS), 0.25 + 0.75 *. i2f((y + x) % 2), 0.5));
        prfillrgba(r, g, b, 255);
        prrect(i2f(CELLEXT * x), i2f(CELLEXT * y), i2f(CELLEXT), i2f(CELLEXT))
    };

    for(CELLIXS, drawcell)
};

// helper - format a float for diagnostic purposes
ffmt(f, places)
{
    s = f2s(f);
    p = strfind(s, ".");

    if(strfind(s, "E") < strlen(s),
    {
        iif(f <. 0.0, "-E", "E")
    },
    {
        if(endswith(s, ".0"),
        {
            strdrop(-2, s)
        },
        {
            strtake(min(strlen(s), p + places + sign(places)), s)
        })
    })
};

// draw player info for the current mode
drawinfo(pbox:*Player)
{
    p = *pbox;

    // get diag text for mode
    s = switch(*xray,
    [
        #none: { "" },
        #vel:
        {
            pv = playervel(pbox);
            ffmt(pv.0 *. 1000.0, 1) + "," + ffmt(pv.1 *. 1000.0, 1)
        },
        #cells:
        {
            (cx, cy) = poscellxy(p.pos);
            i2s(cx) + "," + i2s(cy) + "(" + i2s(size(*(cells[cy][cx]))) + ")"
        },
        #health:
        {
            ffmt(p.health, 3)
        }
    ]);

    // print text at player position
    prtextfont(diagfont());
    prfill(0);
    prtext(s, p.pos.0 -. prtextwidth(s) /. 2.0, p.pos.1 + 4.0);

    // if we're doing velocity, draw velocity vector as a seres of red dots
    when(*xray == #vel,
    {
        prfill(0xff0000);
        pv = playervel(pbox);
        drawdot(f) { prellipse(p.pos.0 + f *. pv.0, p.pos.1 + f *. pv.1, 6.0, 6.0)};
        m = sqrt(mag(pv)) *. 200.0;
        cycle(0.0, { $0 <. m }, { drawdot($0); $0 + 20.0 })
    });
};

// -----------------

// splash screen

// state
starting = box(true);       // true on initial splash, false after
splashing = box(true);      // true when we're showing splash text
splashrc = box(0, 0);       // current (row, col) of visible splash text

// (left, top) for text - recalculated once we load a font
(splashleft, splashtop) = (box(SMALLEXT /. 8.0), box(SMALLEXT /. 8.0));

(SPLASHBG, SPLASHFG) = (0x3C4A4A, 0xE1E5AB);

LINEHEIGHT = 36.0;          // processing doesn't have an easy way of getting this per font

CHARDELAY = 25;             // per-character delay in millis
LINEDELAY = 500;            // per-line delay in millis

INTRO =
[
    "bouncy balls",
    "",
    "* drag to move",
    "* drag and release to throw",
    "* release outside window for slingshot",
    "* click to add balls",
    "",
    "* balls die of old age",
    "* collisions restore youth",
    "",
    "* 'x' to cycle diagnostics",
    "* +/- to add/remove tasks",
    "* other keys redisplay this screen",
    "",
    "click or any key to start"
];

// draw splash screen
drawsplash()
{
    if(*starting,
    {
        prbackground(SPLASHBG)
    },
    {
        // after startup, overlay
        drawplayers();
        (r,g,b) = unpack(SPLASHBG);
        prfillrgba(r, g, b, 160);
        prrect(0.0, 0.0, i2f(W), i2f(H))
    });

    // print current text
    prfill(SPLASHFG);
    printintro();

    ()
};

// print the amount of intro text specified by splashrc,
// starting from location (splashleft, splashtop)
printintro()
{
    (r, c) = *splashrc;

    // helper - print a given line of the intro
    printline(i)
    {
        line = INTRO[i];
        y = *splashtop + LINEHEIGHT *. i2f(i);

        // helper - print a given char of the current line
        printchar(j)
        {
            char = strtake(1, strdrop(j, line));
            x = *splashleft + prtextwidth(strtake(j, line));
            prtext(char, x, y)
        };

        // print either all chars on line, or uncovered chars of final line
        for(count(iif(i == r, c + 1, strlen(INTRO[i]))), printchar)
    };

    // set font, then print all uncovered lines
    prtextfont(splashfont());
    for(count(r + 1), printline)
};

// start the splash task - reveals intro text on a timer
startsplash()
{
    // set (splashleft, splashtop) to center text within window
    prtextfont(splashfont());
    textwidth = reduce(fmax, 0.0, INTRO | prtextwidth);
    textheight = i2f(size(INTRO)) *. LINEHEIGHT;
    splashleft := (i2f(W) -. textwidth) /. 2.0;
    splashtop := (i2f(H) -. textheight) /. 2.0;

    spawn
    {
        // step splashrc through our intro text, with per-char and per-line pauses
        typeline(i)
        {
            typechar(j)
            {
                when(*starting, {sleep(CHARDELAY)});
                splashrc := (i, j);
            };

            line = INTRO[i];
            for(count(strlen(line)), typechar);
            when(!empty(line) && {*starting}, {sleep(LINEDELAY)})
        };

        for(index(INTRO), typeline)
    }
};

// 1. toggle splashing state variable on/off.
// 2. first time splash is turned off, start gameplay
togglesplash()
{
    splashing <- not;

    when(*starting && {!*splashing},
    {
        startplay();
        starting := false
    })
};

// ---------

// fonts

fonts : *[Symbol:Font] = box([:]);

loadfonts()
{
    fonts <- { mapset($0, #splash, prloadfont("data/AmericanTypewriter-24.vlw")) };
    fonts <- { mapset($0, #diag, prloadfont("data/SansSerif.plain-12.vlw")) };
};

splashfont() { (*fonts)[#splash] };
diagfont() { (*fonts)[#diag] };

// -----------------

// processing

// click handler - left click spawns a new batch of players, right click
// kills players under mouse
click()
{
    switch(find([#LEFT,#RIGHT], prmousebutton()),
    [
        0: { newbatch(v2(prmouse()), BATCH) },
        1: killplayers,
        2: { () }
    ])
};

killplayers()
{
    removeplayers(keys(hittest(v2(prmouse()))))
};

// add a calc task
addtask()
{
    calctasks <- inc;

    // if we're getting our first dedicated calc task, spawn it
    when(*calctasks == 1, startturntask);
};

// remove a calc task, set up for in-render turns
// note: dedicated calc task exits when calctasks goes to 0
removetask()
{
    lastrenderturn := nanotime();
    calctasks <- { max(0, dec($0)) }
};

// open window
spawn
{
    propen("bouncy balls - click for more balls, +/- for more/fewer tasks. other keys show help",
    [
        #setup:
        {
            prsize(W, H);
            prnostroke();
            prsmooth();
            loadfonts();
            startsplash()
        },
        #draw:
        {
            if(*splashing, drawsplash, drawplayers)
        },
        #mouseClicked:
        {
            if(*splashing, togglesplash, click)
        },
        #mousePressed:
        {
            when(!*splashing, grab)
        },
        #mouseDragged:
        {
            when(!*splashing, drag)
        },
        #mouseReleased:
        {
            when(!*splashing, release)
        },
        #keyTyped:
        {
            if(*splashing, togglesplash,
            {
                switch(strfind("x+-d", prkey()),
                [
                    0: nextdiag,
                    1: addtask,
                    2: removetask,
                    3: killplayers,
                    4: togglesplash
                ])
            })
        }
    ]);
};
