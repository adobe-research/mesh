
import * from std;
import * from processing;

// serial/parallel quicksort animation

// -------------------------------------------------------------------------
//
// model
//

// app lifecycle is READY -> (WORKING (-> SKIPPING)? -> DONE)+
// see STATETRANS
(READY, WORKING, SKIPPING, DONE) = (0,1,2,3);

DATASIZE = 125;             // size of list to sort
DATAMAX = 500;              // max value for data in list

SORTDELAY = 10;              // millis, used as a baseline

// test data is randomized list of integers
testdata() { draw(DATASIZE, DATAMAX) };

//
// app state (mutable)
//
state = box(READY);         // app state variable
source = box(testdata());   // source for test data, refreshed in reset()
data = box(*source);    // model data to sort

type ThreadId = Long;
type Info = (Int, Int, Int, Int);

sortinfo : *[ThreadId : Info] = box([:]);        // per-task sort info

parallel = box(false);      // we toggle parallel sort on/off. (start serial)

//
// spawn a task to sort data and mark state DONE on completion.
// this is the action for READY -> WORKING; see STATETRANS.
//
startwork()
{
    spawn
    {
        animsort(data, (<));
        state := DONE
    }
};

//
// reset model data, toggle between serial and parallel,
// and start a new sort.
// this is the action for DONE -> WORKING; see STATETRANS.
//
// Note that we tween the data list to its new state using
// an imperative stepper loop with no explicit synchronization.
//
again()
{
    // generate new dataset
    source := testdata();

    // animation: tween data list to new values
    spawn
    {
        tween(old, new) {
            diff = new - old;
            guard(abs(diff) <= 1, new, {old + diff / 2})
        };

        src = *source;

        while({ *data != src }, {
            data <- { zip($0, src) | tween };
            sleep(100)
        });

        // start a new sort
        startwork()
    }
};

// app state transition table.
// used by mouse click handler, below
STATETRANS =
[
    READY: (#state: WORKING, #action: startwork),
    WORKING: (#state: SKIPPING, #action: {()}),
    SKIPPING: (#state: SKIPPING, #action: {()}),
    DONE: (#state: WORKING, #action: again)
];

//
// sort passed boxed list in place, using the passed comparison function.
//
// Note that the boxed list will be accessed by the animation task
// while it is being sorted by one (if serial) or many (if parallel)
// computation tasks. No inversion of control or explicit synchronization
// is needed, because
// * all updates are transactional
// * all snapshots are immutable
//
animsort(listbox:*[Int], cmp:(Int, Int)->Bool)
{
    // helper - sleep if not skipping
    nap(delay)
    {
        when(*state != SKIPPING, { sleep(delay) });
    };

    // helper - swaps list values at the given positions
    swap(i, j)
    {
        listbox <- { listsets($0, [i, j], [$0[j], $0[i]]) };
        nap(SORTDELAY);
    };

    // helper - sorts list between given positions
    subsort(l, r)
    {
        when(r > l,
        {
            // use rightmost value as pivot
            pivot = (*listbox)[r];

            // update animation info
            sortinfo <- { mapset($0, taskid(), (l, r, l, pivot)) };

            // partition our region of list around pivot
            partstep(storepos, i)
            {
                guard(!cmp((*listbox)[i], pivot), storepos,
                {
                    when(storepos != i, { swap(storepos, i) });

                    // update animation info
                    sortinfo <- { mapset($0, taskid(), (l, r, storepos + 1, pivot)) };
                    storepos + 1
                })
            };

            // find midpoint
            mid = evolve(l, partstep, range(l, r - l));

            // when
            when(r != mid && { lst = *listbox; lst[r] != lst[mid] }, {
                swap(r, mid)
            });

            // animation: pause for breath, then clear animation info
            nap(SORTDELAY * 16 * f2i(i2f(r - l) ^. 0.25));
            sortinfo <- { mapdel($0, taskid()) };

            // sort the partitions
            parts = [(l, mid - 1), (mid + 1, r)];

            // NOTE: only concurrency-sensitive code
            iif(*parallel, pfor, for)(parts, subsort)
        });
    };

    // sort the whole list
    subsort(0, size(*listbox) - 1)
};

// -------------------------------------------------------------------------
//
// view
//

// layout and colors
(W,H) = (800, 450);

HBORDER = i2f(W / DATASIZE / 2);
VBORDER = 20.0;

ITEMW = (i2f(W) -. 2.0 *. HBORDER) /. i2f(DATASIZE);
ITEMW2 = ITEMW /. 2.0;
ITEMH = (i2f(H) -. 2.0 *. VBORDER) /. i2f(DATAMAX);
ITEMH2 = ITEMH /. 2.0;

ITEMDIAM = fmax(1.0, ITEMW *. 0.6);
ITEMRAD = ITEMDIAM /. 2.0;

SWAPDIAM = 20.0;
SPOTDIAM = 120.0;

BG = 0;
ITEMFILL = 0xF0F8FF;

// different spot colors for serial/parallel
SPOTFILL = [false: (0xFF, 0xFF, 0x7F, 0x60), true: (0xAF, 0xAF, 0xFF, 0x60)];

// processing setup
setupfunc()
{
    prsize(W, H);
    prnostroke();
    prsmooth();
    prbackground(BG);
};

// draw handler, gets called at 60hz
drawfunc()
{
    x(v) { HBORDER + i2f(v) *. ITEMW + ITEMW /. 2.0 };
    y(v) { VBORDER + i2f(v) *. ITEMH + ITEMH /. 2.0 };

    prbackground(BG);

    // draw scatter plot of list data
    prfill(ITEMFILL);
    list = *data;
    count(DATASIZE) | { prellipse(x($0), y(list[$0]), ITEMDIAM, ITEMDIAM) };

    // use per-task sort info to overlay regions
    srtinfo = *sortinfo;

    drawregion(tid)
    {
        (lf, rt, pivotx, pivoty) = srtinfo[tid];

        [r,g,b] = filet(rotate([0x7F, 0x7F, 0, 0, 0, 0], lf % 6), 2) | sum;
        (x0, xmid, x1) = (x(lf) -. ITEMW2, x(pivotx) -. ITEMW2, x(rt) + ITEMW2);
        (y0, ymid, y1) = (y(-5), y(pivoty) -. ITEMH2, y(DATAMAX + 5));

        prfillrgba(r, g, b, 0x60);
        prrect(x0, y0, xmid -. x0, ymid -. y0);
        prrect(xmid, ymid, x1 -. xmid, y1 -. ymid);

        prfillrgba(r, g, b, 0x30);
        prrect(xmid, y0, x1 -. xmid, ymid -. y0);
        prrect(x0, ymid, xmid -. x0, y1 -. ymid);
    };

    for(keys(srtinfo), drawregion)
};

// mouse click handler.
// we move to next app state via STATETRANS table defined in model
mouseclick()
{
    action = do {
        next = STATETRANS[*state];
        state := next.state;
        next.action
    };

    action()
};

// keypress handler
keytyped()
{
    when(prkey() == "p", { parallel <- not })
};

// open processing window
propen("animated quicksort - click to start/skip/repeat, p to toggle serial/parallel",
[
    #setup: setupfunc,
    #draw: drawfunc,
    #mouseClicked: mouseclick,
    #keyTyped: keytyped
]);

printstr("***\n*** $q to close shell\n***");
