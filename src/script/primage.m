
import processing;

//
// image browser with background loading
//

//
// FLICKR API
//

// uses predefined XML node type:
// type XNode = (#name:String, #attrs:[Symbol : String], #elems:[XNode]);

APIKEY = "3ca11f62d7c50c6527d0b4fc87916d36";
RESTAPI = "http://api.flickr.com/services/rest";
SEARCH = RESTAPI + "/?method=flickr.photos.search&api_key=" + APIKEY;
GETSIZES = RESTAPI + "/?method=flickr.photos.getSizes&api_key=" + APIKEY;

//
// search on a given tag, per-page count, and page number.
// returns array of XNodes from XML
//
search(tag, quant, page) -> [XNode]
{
    url = SEARCH + "&tag_mode=all&tags=" + tag +
        "&per_page=" + i2s(quant) + "&page=" + i2s(page + 1) +
        "&sort=interestingness-desc";

    rsp = httpgetxml(url);

    guard(rsp.name != "rsp" || {rsp.attrs[#stat] != "ok"}, [], {
        rsp.elems[0].elems
    })
};

// size modifier for flickr image filenames
SIZEPARAM = (#thumb: "t", #small: "m", #med: "", #big: "b");

//
// builds url from an XNode of picture info and a size param
//
picurl(picinfo:XNode, sizeparam:String)
{
    a = picinfo.attrs;

    "http://farm" + a[#farm] + ".static.flickr.com/" + a[#server] + "/" +
        a[#id] + "_" + a[#secret] +
        guard(empty(sizeparam), "", {"_" + sizeparam}) +
        ".jpg"
};

// get thumbnail url from picture info
thumburl(info)
{
    picurl(info, SIZEPARAM.thumb)
};

// get url for biggest size available that fits in passed dims
bestsizeurl(info:XNode, w, h)
{
    url = GETSIZES + "&photo_id=" + info.attrs[#id];
    rsp = httpgetxml(url);

    if(rsp.name != "rsp" || {rsp.attrs[#stat] != "ok"}, {
        // default to thumb url if something bad happened to query
        thumburl(info)
    }, {
        sizeinfos = rsp.elems[0].elems;
        dims = sizeinfos | { n:XNode => (s2i(n.attrs[#width]), s2i(n.attrs[#height])) };

        fitixs = where(dims, { picw, pich => picw <= w && { pich <= h } });

        if(empty(fitixs), {
            // note: default to thumb url if nothing fits our dims
            thumburl(info)
        }, {
            maxarea(i, j) {
                cur = dims[i];
                next = dims[j];
                iif(cur.0 * cur.1 >= next.0 * next.1, i, j)
            };

            bestix = evolve(first(fitixs), maxarea, rest(fitixs));
            sizeinfos[bestix].attrs[#source]
        })
    })
};

// convenience - get XML over http
httpgetxml(url:String) -> XNode
{
    parsexml(httpget(url))
};

//
// SPINNER
//

// keep a single spinner going for busy animations

spinangle = box(0.0);           // spinner angle of rotation

TWO_PI = 6.2831855;
SPINARC = TWO_PI /. 12.0;       // 'searchlight' spread
SPINHZ = 0.5;                   // spins per second
SPINUPDATEHZ = 60.0;            // update rate

SPININC = TWO_PI *. SPINHZ /. SPINUPDATEHZ;

// start the spinner task, called from setup.
startspinner()
{
    spawn
    {
        while(prisopen, {
            sleep(f2i(1000.0 /. SPINUPDATEHZ));
            spinangle <- { ($0 + SPININC ) %. TWO_PI}
        })
    }
};

// generate color from position
spotcolor(x, y)
{
    filet(rotate([0x7F, 0x7F, 0x7F, 0x7F, 0x7F, 0], f2i(x *. y) % 6), 2) | sum
};

// draw spinner into a given rect. spinner size varies with rect size,
drawspin(x, y, w, h)
{
    [r, g, b] = spotcolor(x, y);                // spotlight color from rect origin
    (cx, cy) = (x + w /. 2.0, y + h /. 2.0);    // center (spotlight origin)
    ang = *spinangle;                           // angle (spotlight direction)
    rad = 5.0 *. w ^. 0.5;                      // radius (spotlght extent)
    segw = 4;                                   // segment width in pixels
    nsegs = f2i(rad) / segw;                    // number of segments to draw

    // draw a pie-slice starting at spot center and radiating out (i/nsegs)
    // of the way to rad. the bigger the slice, the more transparent.
    drawseg(i)
    {
        prfillrgba(r, g, b, 0x40 * (nsegs - i) / nsegs);
        segrad = i2f(i * segw);
        prarc(cx, cy, segrad, segrad, ang, ang + SPINARC)
    };

    // draw translucent segs from big to small, so the center is brightest
    reverse(count(nsegs)) | drawseg;
    ()
};

//
// APP CONSTANTS
//

WAITBG = 0x0D1F28;

(MAXTHUMBW, MAXTHUMBH) = (100, 100);
(HBORDER, VBORDER) = (14, 8);
(CELLW, CELLH) = (MAXTHUMBW + 2 * HBORDER, MAXTHUMBH + 2 * VBORDER);

(GRIDCOLS, GRIDROWS) = (7, 8);
PERPAGE = GRIDROWS * GRIDCOLS;

(W,H) = (GRIDCOLS * CELLW, GRIDROWS * CELLH);

(THUMBVIEW, FULLVIEW) = (false, true);

//
// APP STATE
//

tag = box("rothko");            // search tag - watched, state resets on changes

viewstate = box(THUMBVIEW);     // either viewing thumbnails or a full pic
corner = box((0,0));            // left/top position of thumbnail sheet
selindex = box(0);              // selected (full) image index

picinfo : Box([XNode]) = box([]);           // list of picture info xml nodes
thumbs : Box([XNode : Image]) = box([:]);   // map of infos to thumbnail images
fulls : Box([XNode : Image]) = box([:]);    // map of infos to full-size images

pageloads : Box([Int : Unit]) = box([:]);     // keys are indexes of loading/loaded pages
imageloads : Box([XNode : Unit]) = box([:]);   // keys are infos of loading/loaded full images

// reset app state (atomically)
// setup() sets a watcher on tag, which calls this if tag has changed
reset()
{
    do {
        viewstate := THUMBVIEW;
        picinfo :=  [];
        thumbs := [:];
        fulls := [:];
        pageloads := [:];
        imageloads := [:];
        corner := (0,0)
    }
};

// ensure that thumbnails are available for all onscreen cells.
// runs whenever app contact sheet is dragged.
// if any new pages of thumbs need to be loaded (i.e., if empty
// cells are showing and downloads are not already in progress),
// spawn a task to perform parallel downloads.
//
ensurethumbs()
{
    // background task will download pages of thumbs in parallel
    for(getnewpages(), { page => spawn { loadpage(page) } })
};

// calculate pages to load, grabbing entries in global loader map
getnewpages()
{
    do {
        pages = *pageloads;                     // current pageloader map
        needindex = cell2index(hicell());       // index of highest onscreen cell
        needpage = needindex / PERPAGE;         // index of page that cell is on
        hipage = reduce(max, -1, keys(pages));  // index of highest load(ed|ing) page

        // we need any pages between the highest loaded and highest visible
        pagelist = range(hipage + 1, max(0, needpage - hipage));

        // add entries for these pages in pageloads
        pageloads <- { mapsets($0, pagelist, [()]) };

        pagelist
    }
};

// load a page of image info from flickr,
// then download page's thumbnails in parallel
loadpage(p)
{
    // do flickr search
    curtag = *tag;
    newinfos = search(curtag, PERPAGE, p);

    // tag may have changed while we were searching. if so, forget it.
    // otherwise, add image infos to global list, and do concurrent download.
    // downloaded images are retained even if tag has changed during download.
    when(*tag == curtag, {
        picinfo <- { $0 + newinfos };
        for(newinfos, { info =>
            spawn {
                img = prloadimage(thumburl(info));
                thumbs <- { mapset($0, info, img) }
            }
        })
    });
};

// if not already running, spawn a new task to download requested image
// in the biggest available size that fits in our window.
// Note that size discovery involves a chat with flickr, which we can do
// synchronously, since we're in a background task.
ensurefull(info)
{
    // test and (maybe) update imageloads map
    needimage = tau(imageloads, { !iskey($0, info) }, { mapset($0, info, ()) }).0;

    when(needimage, {
        spawn {
            // get best url, load and add image to map
            url = bestsizeurl(info, W, H);
            img = prloadimage(url);
            fulls <- { mapset($0, info, img) };

            // now clear loader entry
            imageloads <- { mapdel($0, info) }
        }
    })
};

// index for given cell position
cell2index(cellx, celly)
{
    pagenum = cellx / GRIDCOLS;
    pageoff = (celly * GRIDCOLS) + (cellx % GRIDCOLS);
    PERPAGE * pagenum + pageoff
};

// cell position for given index
index2cell(i)
{
    (pagenum, pageoff) = (i / PERPAGE, i % PERPAGE);
    (pagenum * GRIDCOLS + i % GRIDCOLS, pageoff / GRIDCOLS)
};

// onscreen rectangle for cell position
cell2rect(cellx, celly)
{
    (l, t) = *corner;
    [x, y, w, h] = [l + cellx * CELLW, t + celly * CELLH, CELLW, CELLH] | i2f;
    (x, y, w, h)
};

// cell position for onscreen point
pt2cell(x, y)
{
    (l, t) = *corner;
    ((-l + x) / CELLW, (-t + y) / CELLH)
};

// center a figure of a given width and height in/on a given ground rect (x, y, w, h)
center(ground:(Double, Double, Double, Double), figw:Int, figh:Int)
{
    (fw, fh) = ( i2f(figw), i2f(figh) );
    [dx, dy] = [ground.2 -. fw, ground.3 -. fh] | { $0 /. 2.0 };
    (ground.0 + dx, ground.1 + dy, fw, fh)
};

// draw an image centered in a given rectangle
drawimg(rect, img)
{
    (x, y, w, h) = center(rect, primagewidth(img), primageheight(img));
    primage(img, x, y, w, h)
};

// draw wait cursor into the given rectangle
drawwait(rect)
{
    (x, y, w, h) = center(rect, MAXTHUMBW, MAXTHUMBH);
    prfill(WAITBG);
    prrect(x, y, w, h);
    drawspin(x, y, w, h)
};

// draw the thumbnail at a given cell, or a spinner if not loaded yet
drawthumb(cellx, celly)
{
    rect = cell2rect(cellx, celly);
    index = cell2index(cellx, celly);
    infolist = *picinfo;

    if(size(infolist) <= index, {
        drawwait(rect)
    }, {
        picmap = *thumbs;
        info = infolist[index];
        if(!iskey(picmap, info), {drawwait(rect)}, {drawimg(rect, picmap[info])})
    })
};

// cell at top, left of screen
locell() { pt2cell(0, 0) };

// cell at bottom, right of screen
hicell()
{
    (x, y) = *corner;
    pt2cell(GRIDCOLS * CELLW - 1, y + GRIDROWS * CELLH - 1)
};

// return list of indexes of all visible cells
visiblecells()
{
    ((locol, lorow), (hicol, hirow)) = (locell(), hicell());
    cells = cross(fromto(locol, hicol), fromto(lorow, hirow));
    filter(cells, { x, y => x >= 0 && { y >= 0 && { y < GRIDROWS } } })
};

// draw all visible thumbnails
drawthumbs()
{
    prbackground(0);
    visiblecells() | drawthumb;
    ()
};

// draw selected full image
drawfull()
{
    prbackground(0);

    drawrect = (0.0, 0.0, i2f(W), i2f(H));
    drawwait() { drawspin(drawrect) };

    i = *selindex;
    infolist = *picinfo;

    if(size(infolist) <= i, drawwait, {
        info = infolist[i];
        picmap = *fulls;

        if(!iskey(picmap, info), {
            drawwait();
            ensurefull(info)
        }, {
            drawimg(drawrect, picmap[info])
        })
    })
};

// settle thumbs view if it's not aligned to contact sheet
settlethumbs()
{
    unsettled() {
        (x, y) = *corner;
        y != 0 || { x % CELLW != 0 || { x > 0 } }
    };

    settle(x, y) {
        coff = x % CELLW;
        ease(n) { sign(n) * max(1, abs(n) / 4) };
        (x - if(x > 0, {ease(x)}, {ease(coff)}), y - ease(y))
    };

    when(!prmousepressed() && unsettled, { corner <- settle })
};

// draw handler for processing, called at 60hz
draw()
{
    switch(*viewstate,
    [
        THUMBVIEW: {
            settlethumbs();
            drawthumbs()
        },

        FULLVIEW: drawfull
    ])
};

// helper - set selection index from mouse location
tracksel()
{
    selindex := cell2index(pt2cell(prmouse()))
};

// mouse click handler, switches between thumbs and full image
mouseClick()
{
    // note: when going back to thumb view, update
    // selection index to reflect moves while in full view
    viewstate <- not;
    when(*viewstate == THUMBVIEW, tracksel);
};

// mouse move handler, tracks selected index while in thumbs view
mouseMove()
{
    when(*viewstate == THUMBVIEW, tracksel)
};

// drag handler, moves top/left location in thumbs view
mouseDrag()
{
    when(*viewstate == THUMBVIEW, {
        (mouse, last) = (prmouse(), prpmouse());
        (dx,dy) = (last.0 - mouse.0, last.1 - mouse.1);
        corner <- { ($0 - dx, $1 - dy) }
    })
};

// processing setup function
setup()
{
    prsize(W, H);
    prnostroke();
    prsmooth();

    startspinner();
    ensurethumbs();

    // initiate thumbnail loads when corner location changes
    react(corner, { _ => ensurethumbs() });

    // clear image data when tag changes
    react(tag, { _ => reset() });
    ()
};

// open processing window
propen("rothko on flickr - drag to slide, click to zoom in/out", [
    #setup: setup,
    #draw: draw,
    #mouseClicked: mouseClick,
    #mouseMoved: mouseMove,
    #mouseDragged: mouseDrag
]);

// convenience for console interaction -
// change search tag, will cause app state reset.
view(t) { tag := t };

// helpful instructions to console
[
    "***",
    "*** > view(\"newtag\")     // to change image set",
    "***",
    "***",
    "*** > $q // to close shell",
    "***"
] | printstr;
