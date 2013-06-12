
import processing;

// time flies example, inspired by
// http://blogs.msdn.com/b/jeffva/archive/2010/03/17/reactive-extensions-for-javascript-the-time-flies-like-an-arrow-sample.aspx

// given a source box, processing function and delay in millis,
// create and return a dest box that reacts to updates in the source
// by updating to the result of f(<new source value>) after the given
// delay. (delayed version of dep() library function)
delay_dep(src, f, millis)
{
    // create dest box, init from current source value
    dest = box(f(get(src)));

    // on src change, update dest with f(val) after delay
    react(src, { val =>
        spawn {
            sleep(millis);
            dest := f(val)
        }
    });

    // return dest box
    dest
};

// message string
MSG = "time flies like an arrow";

// global mouse position, updated on mouse moves
mpos = box(0, 0);

// for a given index i into string s, create a dependent box
// that tracks mouse position plus horizontal offset, after
// a delay proportional to index.
makedep(i, s)
{
    // given base position, return offset position of s(i)
    ipos(x,y) {
        xoff = round(prtextwidth(strtake(i, s)));
        (x + xoff, y)
    };

    // return a new box subscribed to i-delayed, i-offset mpos
    delay_dep(mpos, ipos, i * 100)
};

// global list of boxes reactive to mpos,
// holding mouse positions for each MSG char
locs = count(strlen(MSG)) | { makedep($0, MSG) };

// given i, draw MSG(i) at position held in locs(i)
drawchar(i) {
    (x, y) = get(locs[i]);
    prtext(substr(MSG, i, 1), i2f(x), i2f(y));
};

// open processing window 
propen("time flies",
[
    #setup: {
        // initialize size, font
        prsize(640, 640);
        prnostroke();
        prsmooth();
        prtextfont(prcreatefont("SansSerif", 32));
    },
    #draw: {
        // redraw
        prbackground(0);
        prfill(255);
        count(strlen(MSG)) | drawchar;
        ()
    },
    #mouseMoved: {
        // update global mpos when mouse moves
        mpos := prmouse()
    }
]);
