
import processing;

// -----------------

// bump-based data randomizer. basic idea from
// http://github.com/leebyron/streamgraph_generator/blob/master/BelievableDataSource.java
//

// a Bump is triple of floats representing height, x-displacement and rate (slope)
//
type Bump = (#height: Double, #xoff: Double, #rate: Double);

// generate a bump in the range [x, x + wid).
bump(x, wid) { (
    #height: ln(1.0 /. frand()),           // log keeps things fairly sober
    #xoff: x + frand() *. wid,              // location of bump event in [x, x + wid)
    #rate: fmax(25.0, frand() *. 200.0)     // smaller = narrower/steeper (note: not scaled by wid)
) };

// generate the average quantity of bumps for [x, wid)
AVGBUMPX = 200;     // average x-distance between bumps
bumpgen(x, wid) {
    repeat(wid / AVGBUMPX, {bump(i2f(x), i2f(wid))})
};

// shift bumps a given x-distance
shiftbumps(dx, bumps:[Bump]) {
    bumps | {b:Bump => (#height: b.height, #xoff: b.xoff + dx, #rate: b.rate)}
};

// compute the value at a given x-position, given a list of bumps
bumpedvalue(xpos, bumps:[Bump]) {

    addbump(v, b:Bump) {
        xdist = i2f(xpos) -. b.xoff;    // x distance from bump
        a = xdist /. b.rate;            // adjust for decay
        dy = exp(-.a *. a);             // the amount of bump at i
        v +. b.height *. dy             // displace existing value
    };

    reduce({ addbump($0, $1) }, 0.0, bumps)
};

// -----------------

//
// view
//

(W,H) = (800, 600);
(HBORDER, VBORDER) = (0, 100);

VIEWPORT = (
    #left: i2f(HBORDER), #top: i2f(VBORDER),
    #width: i2f(W - 2 * HBORDER), #height: i2f(H - 2 * VBORDER));

BORDERBG = 0x0D1F28;

// layer colors rotate through a fixed palette
RGB = [0x6480ab, 0x2d5b96, 0x4385e3, 0x88ade3, 0x1e3a63];
layercolor(i) {
    RGB[i % size(RGB)]
};

// brighten a color by a given factor, factor > 1.0 brightens, 0.0 <= factor < 1.0 dims
brighten(rgb, factor) {
    hsb = rgb2hsb(rgb);
    hsb2rgb(hsb.0, hsb.1, fmin(1.0, hsb.2 *. factor))
};

// gratuity
letterbox() {
    skooch = VBORDER / 4;
    prfill(BORDERBG);
    prrect(0.0, 0.0, i2f(W), i2f(VBORDER - skooch));
    prrect(0.0, i2f(H - (VBORDER - skooch)), i2f(W), i2f(VBORDER - skooch));
};

// sum a matrix of floats by column
sumcols(mat:[[Double]]) {
    (nrows, ncols) = (size(mat), size(head(mat)));
    count(ncols) | { c => fsum(count(nrows) | { mat[$0][c] }) }
};

// worker: draw layers on a baseline.
drawlayers(keys:[String], layermap:[String : [Double]], basefunc:[[Double]]->[Double]) {

    layers = maplm(keys, layermap);             // data layers
    base = basefunc(layers);                    // compute baseline
    n = size(head(layers));                    // number of data points
    ixs = count(n);                             // indexes

    iw = fmin(1.0, i2f(W) /. i2f(n));                       // item width
    maxh = reduce(fmax, 0.0, sumcols([base] + layers));     // max total height
    ih = (VIEWPORT.height -. 1.0) /. maxh;                  // item height

    (#left: vl, #top: vt, #width: vw, #height: vh) = VIEWPORT;
    vb = vt + vh;

    // worker - draws the shape for a given layer
    drawlayer(base:[Double], layer:[Double]) {

        x(i) { vl +. iw *. i2f(i) };

        bots = ixs | { vb -. ih *. base[$0] };
        tops = ixs | { bots[$0] -. ih *. layer[$0] + 1.0 };

        prbeginshape();
        reverse(ixs) | {i => prvertex(x(i), tops[i])};
        ixs | {i => prvertex(x(i), bots[i])};
        prendshape();
    };

    // draw layers from bottom to top, accumulating baseline and label x-pos.
    // could do this functionally with reduce(stacker, (base, max...), count(...))
    baseaccum = box(base);
    labxaccum = box(max(HBORDER, 25));

    stacker(i) {

        // for now, just write each key across the top as a legend
        label = keys[i];
        labx = get(labxaccum);
        prfill(brighten(layercolor(i), 1.2));
        prrect(i2f(labx), i2f(VBORDER - 60), 10.0, 10.0);
        prtext(label, i2f(labx + 15), i2f(VBORDER - 50));
        update(labxaccum, { $0 + round(prtextwidth(label + " ") + 20.0) });

        // draw this layer, and add it to the accumulating baseline
        layer = layers[i];
        prfill(layercolor(i));
        drawlayer(get(baseaccum), layer);
        update(baseaccum, { b => ixs | { b[$0] +. layer[$0] } });
    };

    // draw
    prbackground(0);
    letterbox();
    index(layers) | stacker;
    ()
};

// baseline generators

// flat baseline is just zero all the way across
flatbase(layers:[[Double]]) {
    rep(size(head(layers)), 0.0)
};

// centered baseline is, for a given x, the centerline minus half the stack height at that x
centeredbase(layers:[[Double]]) {
    heights = sumcols(layers);
    maxh = reduce(fmax, 0.0, heights);
    index(head(layers)) | { (maxh -. heights[$0]) /. 2.0 }
};

// min-wiggle baseline
// http://github.com/leebyron/streamgraph_generator/blob/master/MinimizedWiggleLayout.java
minwigglebase(layers:[[Double]]) {
    nlayers = size(layers);
    nitems = size(head(layers));
    calc(x) {
        contrib(y) { (i2f(nlayers - y) -. 0.5) *. layers[y][x] };
        fsum(count(nlayers) | contrib) /. i2f(nlayers)
    };
    heights = count(nitems) | calc;
    maxh = reduce(fmax, 0.0, heights);
    count(nitems) | { (maxh -. heights[$0]) /. 2.0 }
};

// ------------

//
// app vars, hookup to processing
//

BASEFUNCS = [flatbase, centeredbase, minwigglebase];

BUFSIZE = 1000;
NSERIES = 10;
DATAKEYS = range(1, NSERIES) | i2s | { "Series " + $0 };

PAUSE = 1000/60;  // ms

// app state
datamap : *[String:[Double]] = box(assoc(DATAKEYS, [[]]));
style = box(2);
paused = box(false);
nsamples = box(3);

datasize(map) {
    size(head(values(map)))
};

// add a new set of samples to global data map.
// returns the number of old samples dropped.
addsamples(samplemap : [String : [Double]]) {
    do {
        dmap = get(datamap);
        ncur = datasize(dmap);
        nnew = datasize(samplemap);
        dropped = max(0, ncur + nnew - BUFSIZE);
        curdata = maplm(DATAKEYS, dmap);
        newdata = maplm(DATAKEYS, samplemap);
        data = zip(curdata, newdata) | { drop(dropped, $0 + $1) };
        put(datamap, mapsets(dmap, DATAKEYS, data));
        dropped
    }
};

// spool randomized data into addsamples()
animate() {
    spawn {

        initbumps() {
            bumpgen(-BUFSIZE, 3 * BUFSIZE)
        };

        nextbumps(curbumps) {
            curbumps + bumpgen(2 * BUFSIZE, BUFSIZE)
        };

        slide(n, bumps) {
            shifted = shiftbumps(i2f(-n), bumps);
            filter(shifted, {b:Bump => b.xoff >=. i2f(-BUFSIZE)})
        };

        cycle((BUFSIZE, repeat(NSERIES, initbumps)), {_ => true}, { off, bumplists:[[Bump]] =>

            sleep(PAUSE);

            // wait until/unless *paused is true
            await(paused, not);

            newix = datasize(get(datamap));
            n = get(nsamples);
            newvals = bumplists | { bumps => range(newix, n) | { bumpedvalue($0, bumps) } };
            shift = addsamples(assoc(DATAKEYS, newvals));

            if(off - n <= 0, {
                (BUFSIZE - off - n, bumplists |: { slide(shift, nextbumps($0)) })
            }, {
                (off - n, bumplists |: { slide(shift, $0) })
            })
        })
    }
};

// interaction functions
pause() { paused <- not };

faster() { nsamples <- inc };
slower() { nsamples <- { max(1, dec($0)) } };

changestyle() { update(style, {inc($0) % size(BASEFUNCS)}); () };

// open a processing window
propen("streamgraph - +/- to change sampling rate, p to pause, s to change style", [
    #setup: {
        prsize(W, H);
        prnostroke();
        prsmooth();
        prbackground(0);
        animate()
    },
    #draw: {
        drawlayers(DATAKEYS, get(datamap), BASEFUNCS[get(style)])
    },
    #mouseClicked:
        changestyle,
    #keyTyped: {
        switch(strfind("+-ps", prkey()), [
            0: faster,
            1: slower,
            2: pause,
            3: changestyle,
            4: {()}
        ])
     }
]);
