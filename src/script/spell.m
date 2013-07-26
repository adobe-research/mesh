// http://norvig.com/spell-correct.html

import bench;   // benchmarking utilities

// load word sample population and count
print("loading word counts...");
readwords(path) { strsplit(tolower(readfile(path)), "\\W+") };
words = readwords("data/big.txt");
dict = counts(words);

// print stats
("loaded, words:", size(words), "mean count:", avg(values(dict)));

// letters as single-character strings
alphabet = strcut("abcdefghijklmnopqrstuvwxyz", count(26));

//
// for a given word, generate a list of all variations
// (deletions, transpositions, replacements and insertions)
// with edit distance 1
//
edits1(word)
{
    // list of pairs, word split at each position
    splits =  count(strlen(word) + 1) | {i => (strtake(i, word), strdrop(i, word))};

    // splits with right fragment of at least 1 char
    splits1 = filter(splits, {a, b => strlen(b) > 0});

    // splits with right fragment of at least 2 chars
    splits2 = filter(splits1, {a, b => strlen(b) > 1});

    // list of deletions at each position
    deletes = splits1 | {a, b => a + strdrop(1, b)};

    // list of transpositions at each position
    transposes = splits2 | {a, b => a + substr(b,1,1) + substr(b,0,1) + strdrop(2,b)};

    // list of lists - each position replaced by every letter
    replaces = splits1 | {a, b => alphabet | {c => a + c + strdrop(1, b)}};

    // list of lists - every letter inserted at each position
    inserts = splits | {a, b => alphabet | {c => a + c + b}};

    // return a single flat list
    flatten([deletes, transposes] + replaces + inserts)
};

// filter a list of words by their presence in dict
known(words)
{
    filter(words, {w => iskey(dict, w)})
};

//
// for a given word, generate all edits of distance 2
// that are based on known-word edits of distance 1
//
known_edits2(word)
{
    unique(flatten(edits1(word) | { known(edits1($0)) }))
};

//
// return our best shot at a correct version
// of the passed word
//
correct(word)
{
    // candidates are first of
    // (original word, edit-1 words, edit-2 words)
    // that yields a nonempty list
    candidates = evolve_while(empty, [], {_, f => f()},
        [ {known([word])}, {known(edits1(word))}, {known_edits2(word)} ]);

    // find word count of most popular candidate
    hi = evolve(0, max, maplm(candidates, dict));

    // return first word with highest nonzero count,
    // or fall back to original
    guard(hi == 0, word, {
        candidates[first_where({ dict[$0] == hi }, candidates)]
    })
};

// --------------------

//
// test harness
//

// record holds stats from a test run
type Stats = (tests:Int, misses:Int, unknowns:Int);

//
// runs list of (target, misspells) test cases
// in parallel or serial, with or without verbosity.
// returns stats record
//
spelltest(cases:[String : String], parallel:Bool, verbose:Bool) -> Stats
{
    // stat accumulators
    tests = box(0);
    misses = box(0);
    unknowns = box(0);

    // test case runner. goal is correct spelling,
    // inputs is a space-delimited list of misspellings
    testcase(goal:String, inputs:String)
    {
        // test runner for a single input
        testword(input:String)
        {
            // update test counter
            update(tests, inc);

            // run our correction function
            corrected = correct(input);

            // print diags
            if(corrected == goal,
            {
                // found the goal spelling - print verbose diags
                when(verbose, {print(word: input, corrected: corrected)})
            },
            {
                // missed - update miss counter
                update(misses, inc);

                // is it a known word?
                known = iskey(dict, goal);

                // if not, update unknown result counter
                when(!known, {update(unknowns, inc)});

                // print verbose diags
                when(verbose,
                {
                    print(word: input,
                        corrected: corrected,
                        goal: goal,
                        unknown: !known)
                })
            })
        };

        // test em
        strsplit(inputs, " ") | testword
    };

    // run tests, either serially or in parallel
    if(parallel, {
        pforn(entries(cases), testcase, availprocs() * 2)
    }, {
        for(entries(cases), testcase)
    });

    // return stats
    (tests: get(tests), misses: get(misses), unknowns: get(unknowns))
};

//
// test sets
//
tests1 = [ "access": "acess", "accessing": "accesing", "accommodation":
"accomodation acommodation acomodation", "account": "acount", "address":
"adress adres", "addressable": "addresable", "arranged": "aranged arrainged",
"arrangeing": "aranging", "arrangement": "arragment", "articles": "articals",
"aunt": "annt anut arnt", "auxiliary": "auxillary", "available": "avaible",
"awful": "awfall afful", "basically": "basicaly", "beginning": "begining",
"benefit": "benifit", "benefits": "benifits", "between": "beetween", "bicycle":
"bicycal bycicle bycycle", "biscuits":
"biscits biscutes biscuts bisquits buiscits buiscuts", "built": "biult",
"cake": "cak", "career": "carrer",
"cemetery": "cemetary semetary", "centrally": "centraly", "certain": "cirtain",
"challenges": "chalenges chalenges", "chapter": "chaper chaphter chaptur",
"choice": "choise", "choosing": "chosing", "clerical": "clearical",
"committee": "comittee", "compare": "compair", "completely": "completly",
"consider": "concider", "considerable": "conciderable", "contented":
"contenpted contende contended contentid", "curtains":
"cartains certans courtens cuaritains curtans curtians curtions", "decide": "descide", "decided":
"descided", "definitely": "definately difinately", "definition": "defenition",
"definitions": "defenitions", "description": "discription", "desiccate":
"desicate dessicate dessiccate", "diagrammatically": "diagrammaticaally",
"different": "diffrent", "driven": "dirven", "ecstasy": "exstacy ecstacy",
"embarrass": "embaras embarass", "establishing": "astablishing establising",
"experience": "experance experiance", "experiences": "experances", "extended":
"extented", "extremely": "extreamly", "fails": "failes", "families": "familes",
"february": "febuary", "further": "futher", "gallery": "galery gallary gallerry gallrey",
"hierarchal": "hierachial", "hierarchy": "hierchy", "inconvenient":
"inconvienient inconvient inconvinient", "independent": "independant independant",
"initial": "intial", "initials": "inetials inistals initails initals intials",
"juice": "guic juce jucie juise juse", "latest": "lates latets latiest latist",
"laugh": "lagh lauf laught lugh", "level": "leval",
"levels": "levals", "liaison": "liaision liason", "lieu": "liew", "literature":
"litriture", "loans": "lones", "locally": "localy", "magnificent":
"magnificnet magificent magnifcent magnifecent magnifiscant magnifisent magnificant",
"management": "managment", "meant": "ment", "minuscule": "miniscule",
"minutes": "muinets", "monitoring": "monitering", "necessary":
"neccesary necesary neccesary necassary necassery neccasary", "occurrence":
"occurence occurence", "often": "ofen offen offten ofton", "opposite":
"opisite oppasite oppesite oppisit oppisite opposit oppossite oppossitte", "parallel":
"paralel paralell parrallel parralell parrallell", "particular": "particulaur",
"perhaps": "perhapse", "personnel": "personnell", "planned": "planed", "poem":
"poame", "poems": "poims pomes", "poetry": "poartry poertry poetre poety powetry",
"position": "possition", "possible": "possable", "pretend":
"pertend protend prtend pritend", "problem": "problam proble promblem proplen",
"pronunciation": "pronounciation", "purple": "perple perpul poarple",
"questionnaire": "questionaire", "really": "realy relley relly", "receipt":
"receit receite reciet recipt", "receive": "recieve", "refreshment":
"reafreshment refreshmant refresment refressmunt", "remember": "rember remeber rememmer rermember",
"remind": "remine remined", "scarcely": "scarcly scarecly scarely scarsely",
"scissors": "scisors sissors", "separate": "seperate",
"singular": "singulaur", "someone": "somone", "sources": "sorces", "southern":
"southen", "special": "speaical specail specal speical", "splendid":
"spledid splended splened splended", "standardizing": "stanerdizing", "stomach":
"stomac stomache stomec stumache", "supersede": "supercede superceed", "there": "ther",
"totally": "totaly", "transferred": "transfred", "transportability":
"transportibility", "triangular": "triangulaur", "understand": "undersand undistand",
"unexpected": "unexpcted unexpeted unexspected", "unfortunately":
"unfortunatly", "unique": "uneque", "useful": "usefull", "valuable": "valubale valuble",
"variable": "varable", "variant": "vairiant", "various": "vairious",
"visited": "fisited viseted vistid vistied", "visitors": "vistors",
"voluntary": "volantry", "voting": "voteing", "wanted": "wantid wonted",
"whether": "wether", "wrote": "rote wote"];

//
// test script
//

print("serial");
s = bench({spelltest(tests1, false, true)}); // verbose
//s = bench({spelltest(tests1, false, false)});   // quiet

print("parallel");
p = bench({spelltest(tests1, true, true)});  // verbose
//p = bench({spelltest(tests1, true, false)});    // quiet

// print results
print("serial", s);
print("parallel", p);
