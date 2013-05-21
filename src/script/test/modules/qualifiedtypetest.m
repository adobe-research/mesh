
import * from unittest;

import M qualified;
import M into N;
import * from P;
import * from Q;

type LocalType = (#field : Int, #next : (Double,Double));

a:LocalType = (#field : 0, #next : (0.0, 1.0));
b:M.MyType = (1,10);

c:N.MyType = b;

d:PType = ("abc", "def", "ghi");
e:QType = 4;

assert_equals({ e }, { 4 });
