
// A: import * from M
// B: import foo,bar from M
// C: import () from M
// D: import M
// E: import M.(foo,bar)
// F: import M.()
// G: import M as N
// H: import (M as N).(foo,bar)
// I: import (M as N).()
// 
// S: export M.*
// T: export *
// U: <default export>
// V: export ()
// W: export .
// X: export x,y (local)
// Y: export foo,bar (unqualified imports)
// Z: export M.foo,M.bar
//
// Tests postfix:
// _Q: positive test: import qualified
// _U: positive test: import unqualified
// _N: negative test (should fail to compile; not tested here)

import AS_Q;
import AT_Q;
import AU_Q;
import AV_Q;
import AW_Q;
import AX_Q;
import AY_Q;
import AZ_Q;
import BS_Q;
import BT_Q;
import BU_Q;
import BV_Q;
import BW_Q;
import BX_Q;
import BY_Q;
import BZ_Q;
import CS_Q;
import CT_Q;
import CU_Q;
import CV_Q;
import CW_Q;
import CX_Q;
import CY_Q;
import CZ_Q;
import DS_Q;
import DT_Q;
import DU_Q;
import DV_Q;
import DW_Q;
import DX_Q;
import DY_Q;
import DZ_Q;
import ES_Q;
import ET_Q;
import EU_Q;
import EV_Q;
import EW_Q;
import EX_Q;
import EY_Q;
import EZ_Q;
import FS_Q;
import FT_Q;
import FU_Q;
import FV_Q;
import FW_Q;
import FX_Q;
import FY_Q;
import FZ_Q;
import GS_Q;
import GT_Q;
import GU_Q;
import GV_Q;
import GW_Q;
import GX_Q;
import GY_Q;
import GZ_Q;
import HS_Q;
import HT_Q;
import HU_Q;
import HV_Q;
import HW_Q;
import HX_Q;
import HY_Q;
import HZ_Q;
import IS_Q;
import IT_Q;
import IU_Q;
import IV_Q;
import IW_Q;
import IX_Q;
import IY_Q;
import IZ_Q;
import AS_U;
import AT_U;
import AU_U;
import AV_U;
import AW_U;
import AX_U;
import AY_U;
import AZ_U;
import BS_U;
import BT_U;
import BU_U;
import BV_U;
import BW_U;
import BX_U;
import BY_U;
import BZ_U;
import CS_U;
import CT_U;
import CU_U;
import CV_U;
import CW_U;
import CX_U;
import CY_U;
import CZ_U;
import DS_U;
import DT_U;
import DU_U;
import DV_U;
import DW_U;
import DX_U;
import DY_U;
import DZ_U;
import ES_U;
import ET_U;
import EU_U;
import EV_U;
import EW_U;
import EX_U;
import EY_U;
import EZ_U;
import FS_U;
import FT_U;
import FU_U;
import FV_U;
import FW_U;
import FX_U;
import FY_U;
import FZ_U;
import GS_U;
import GT_U;
import GU_U;
import GV_U;
import GW_U;
import GX_U;
import GY_U;
import GZ_U;
import HS_U;
import HT_U;
import HU_U;
import HV_U;
import HW_U;
import HX_U;
import HY_U;
import HZ_U;
import IS_U;
import IT_U;
import IU_U;
import IV_U;
import IW_U;
import IX_U;
import IY_U;
import IZ_U;
