//
// bank balance integrity with transactions
//

type Account = (id:Int, owner:String, balance:*Int);

type AccountMap = [Int : Account];

idgen = box(0);

newId() { postinc(idgen) };

accounts:*AccountMap = box([:]);

//
// create a new account with the given owner and balance,
// and add it to accounts map
//
openAccount(owner, balance)
{
    do {
        id = newId();
        account = (id: id, owner: owner, balance: box(balance));
        accounts <- { m => mapset(m, id, account) };
        account
    }
};

//
// change an account's balance by a given amount. will fail if
// balance would become negative
//
deposit(account:Account, amount:Int)
{
    do {
        sleep(50);  // simulate a long-running operation
        (*account.balance + amount >= 0) && {
            account.balance <- { $0 + amount };
            true
        }
    }
};

// convenience
withdraw(account, amount) { deposit(account, -amount) };

//
// transfer pairs a withdrawal with a deposit, in a transaction
//
transfer(from, to, amount)
{
    // note: logical and takes a block on the right, i.e.
    // leftVal && rightBlock == guard(!leftVal, false, rightBlock)
    //
    do { withdraw(from, amount) && { deposit(to, amount) } }
};

// tester

test()
{
    // clear accounts map
    accounts := [:];

    // open our test accounts
    mark = openAccount("Mark", 100);
    tami = openAccount("Tami", 200);

    print("before transactions: ");
    values(*accounts) | print;

    // task countdown
    countdown = box(2);

    // run a transfer() and a withdraw() concurrently. whichever one is 
    // executed first will cause the other to then fail due to insufficient 
    // funds: if the transfer succeeds, the final balances are mark 50, tami 250; 
    // if the withdrawal succeeds, the final balances are mark 25, tami 200.
    // both happen in practice (due to fluctuations in the timing of the spawns, I think).
    //
    spawn { transfer(mark, tami, 50); countdown <- dec };
    spawn { withdraw(mark, 75); countdown <- dec };

    // wait for tasks to finish
    await(countdown, { $0 == 0 });

    print("after transactions: ");
    values(*accounts) | print;

    // are all accounts non-negative? returns bool
    all(values(*accounts), { a:Account => *a.balance >= 0 })
};

// can call this in shell after $load - winning operation will vary per above
test();
