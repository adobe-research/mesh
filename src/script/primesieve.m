
// Example of generators and data-hiding/encapsulation

// This is a rather stupid (but perhaps interesting) way of generating prime 
// numbers.  We start with a pipeline consisting of a simple incrementing counter. 
// When we retrieve a prime we insert into the pipeline a filter which reject values
// that are multiples of the new prime.  The pipeline is then augmented with a new filter 
// for each additional prime we retrieve.
//
// Usage:
// Call primesieve() to create a new prime generator.  Then call get() repeatedly on the 
// result to get subsequent prime numbers (starting from 2). Call reset() to start over.

primesieve = {

    // private instance method
    generator(init, makenext) {
        state = box(init);
        ( 
            #value: { *state; },
            #next: { state := makenext(*state); *state }
        )
    };

    // private instance method
    filter_relative(pl:(value: ()->Int, next: ()->Int), to) {
        ( 
            value: { pl.value() },
            next: {
                iter({ pl.next() % to == 0 });
                pl.value()
            }
        )
    };

    // private member
    pipeline = box(generator(1, inc));

    ( 
        // public instance method
        get : {
            v = (*pipeline).next();
            pipeline := filter_relative(*pipeline, v);
            v
        },
        
        // public instance method
        reset: {
            pipeline := generator(1, inc);
        }
    )
};

ps = primesieve();
print(ps.get());
print(ps.get());
print(ps.get());
print(ps.get());
print(ps.get());

ps.reset();
print(ps.get());
print(ps.get());
print(ps.get());

// Generate a list of the first 100 primes
print(rep(100, primesieve().get) | run);
