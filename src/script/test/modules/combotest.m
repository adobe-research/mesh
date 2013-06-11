
// A: import M
// B: import foo,bar from M
// C: import () from M
// G: import M into N
// H: import for,bar from M into N
// I: import () form M into N
// 
// T: export *
// U: <default export>
// V: export ()
// W: export . 
// X: export x,y (local)
// Y: export foo,bar (unqualified imports)
//
// Tests postfix:
// _Q: positive test: import qualified
// _U: positive test: import unqualified
// _N: negative test (should fail to compile; not tested here)

import AT_Q;
import AU_Q;
import AV_Q;
import AW_Q;
import AX_Q;
import AY_Q;
import BT_Q;
import BU_Q;
import BV_Q;
import BW_Q;
import BX_Q;
import BY_Q;
import CT_Q;
import CU_Q;
import CV_Q;
import CW_Q;
import CX_Q;
import CY_Q;
import GT_Q;
import GU_Q;
import GV_Q;
import GW_Q;
import GX_Q;
import GY_Q;
import HT_Q;
import HU_Q;
import HV_Q;
import HW_Q;
import HX_Q;
import HY_Q;
import IT_Q;
import IU_Q;
import IV_Q;
import IW_Q;
import IX_Q;
import IY_Q;
import AT_U;
import AU_U;
import AV_U;
import AW_U;
import AX_U;
import AY_U;
import BT_U;
import BU_U;
import BV_U;
import BW_U;
import BX_U;
import BY_U;
import CT_U;
import CU_U;
import CV_U;
import CW_U;
import CX_U;
import CY_U;
import GT_U;
import GU_U;
import GV_U;
import GW_U;
import GX_U;
import GY_U;
import HT_U;
import HU_U;
import HV_U;
import HW_U;
import HX_U;
import HY_U;
import IT_U;
import IU_U;
import IV_U;
import IW_U;
import IX_U;
import IY_U;
