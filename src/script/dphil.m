
//
// dining philosophers using transactions
//

// a Fork is record holding an id and
// an availability flag
//
type Fork = (id: Int, avail: Bool);

// make_fork returns a new fork with unique id
//
make_fork =
{
    // keep a hidden boxed int to generate ids
    idgen = box(0);

    // make_fork returns fork with new id
    { (id: postinc(idgen), avail: true) }
}();

// returns a new fork with same id, toggled availability
toggle_fork(f:Fork) { (id: f.id, avail: !f.avail) };

// a Phil is a name, a list of boxed forks,
// an is-eating flag and a quantity of food
//
type Phil = (name: Symbol, forks: [*Fork], eating: Bool, food: Int);

// create a Phil with the given name, forks
// and food quantity
//
make_phil(name:Symbol, forks:[*Fork], food:Int)
{
    (name: name, forks: forks, eating: false, food: food)
};

// make a new version of phil that is eating
// and has consumed 1 unit of food
started(p:Phil)
{
    (name: p.name,
    forks: p.forks,
    eating: true,
    food: p.food - 1)
};

// attempt to put boxed phil into the eating state.
// success depends on fork availability.
// if successful, forks become unavailable to others,
// eating state becomes true and food quantity
// is decremented. returns success.
//
start_eating(phil:*Phil)
{
    do
    {
        // philosopher's forks
        forks = (*phil).forks;

        // true if boxed fork is available
        avail(f : *Fork) { (*f).avail };

        // if all philosopher's forks are available, start eating
        guard(!all(forks, avail), false,
        {
            // make all forks unavailable
            forks | { $0 <- toggle_fork };

            // update phil to started state
            phil <- started;

            // success
            true
        })
    }
};

// make a new version of phil that is not
// eating
stopped(p:Phil)
{
    (name: p.name,
    forks: p.forks,
    eating: false,
    food: p.food)
};

// if phil is in eating state, take it out
// and release forks.
//
stop_eating(phil:*Phil)
{
    do
    {
        // toggle forks
        (*phil).forks | { $0 <- toggle_fork };

        // update phil to stopped state
        phil <- stopped
    }
};

// do polling cycle on a phil,
// eating if forks are available and then sleeping,
// until food is gone
//
cycle(phil:*Phil, retryMillis:Int, eatMillis:Int, thinkMillis:Int)
{
    while({ (*phil).food > 0 },
    {
        if(start_eating(phil),
        {
            sleep(rand(eatMillis));
            stop_eating(phil);
            sleep(rand(thinkMillis))
        },
        {
            sleep(retryMillis)
        })
    })
};

// print status based on delta between old, new Phil states
status(p : Phil)
{
    print(p.name,
        iif(p.eating, "starts eating", "back to thinking"),
        "food:", p.food)
};

// ----------------------------------

//
// test
//

// philosopher names
names = [#Aristotle, #Bentham, #Carnap, #Deleuze, #Engels];

// number of philosophers
nphils = size(names);

// list of nphils boxed Forks
forks:[*Fork] = repeat(nphils, make_fork) | box;

// initialize a boxed phil from name and starting fork index
init_phil(name, forkix)
{
    myforks = [forks[forkix], forks[(forkix + 1) % nphils]];
    box(make_phil(name, myforks, 10))
};

// list of nphils boxed Phils,
// each holding 2 forks (overlapping with
// adjacent phils)
//
phils:[*Phil] = zip(names, count(nphils)) | init_phil;

// set watcher to print status on phil state change
for(phils, { react($0, status) });

// start a task running cycle() on the given phil
startPhil(p:*Phil) { spawn { cycle(p, 5, 100, 100) } };

// dinner time
phils | startPhil;
