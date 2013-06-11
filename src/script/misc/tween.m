
FPS = 60;
FRAME_DELAY = 1000 / FPS;

//
// Keep a global map of tweening boxes to task ids -
// at a given instant, a box has at most one tween operation.
// New tweens cancel old tweens.
//
tweeners = box([:]);

//
// tweening functions (Penner) - all take
// elapsed time, start value, total change, total duration. 
// TODO start, change should be generic metric space type
// TODO elapsed, dura should be long, once we have op support - will overflow currently
// TODO switch to ^ once we have overloading etc.
//
linear(elapsed, start, change, dura)
{
    start + (change * elapsed) / dura
};

easeInQuad(elapsed, start, change, dura)
{
    ease(i) { f = i2f(i); f *. f };
    (felapsed2, fdura2) = ( ease(elapsed), ease(dura) );
    start + f2i(i2f(change) *. felapsed2 /. fdura2)
};

easeOutQuad(elapsed, start, change, dura)
{
    start + change - easeInQuad(dura - elapsed, start, change, dura)
};

//
// Tween box from start to goal in duration millis, using tweening function twf.
// To tween in a separate task, use spawn(tween(...))
//
tween(box, goal, duration, twf)
{
    // mark start time
    starttime = millitime();

    // set ourselves as tweener for this box, after start time
    tweeners <- { mapset($0, box, taskid()) };

    // save initial value
    start = get(box);

    // calculate total change once
    change = goal - start;

    // loop until either we're at goal, or another task has usurped us
    while({get(box) != goal && { get(tweeners)[box] == taskid() }},
    {
        // pause
        sleep(FRAME_DELAY);

        // long support WIP
        elapsed = l2i(lminus(millitime(), starttime));

        // calc next value
        next = twf(elapsed, start, change, duration);

        // note: if this step goes past goal, converge to goal
        box <- { iif(sign(goal - $0) != sign(goal - next), goal, next) }
    })
};

//
// establish a tweening dependency between source and sink boxes.
// returns watch function, which can be unwatch()ed on source
//
twdep(source, sink, duration, twf)
{
    tw(oldv, newv) { tween(sink, newv, duration, twf) };
    watch(source, tw);
    tw
};

