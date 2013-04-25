
import update from mutate;

b = box(0);

update(b, inc);
print "Loading import_middle (should only happen once)";

get_count() { *b }
