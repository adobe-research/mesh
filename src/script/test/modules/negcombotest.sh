#!/bin/sh

MESH_HOME=../../../..
log=negcombotest.log
rm -f ${log}
touch ${log}

pass=0
fail=0

failures=

for i in *_N.m; do
    moduleName=`basename ${i} .m`
    echo "import ${moduleName}" | ${MESH_HOME}/bin/shell >>${log} 2>&1
    status=$?

    if [ "${status}" = "0" ]; then
        echo "Test ${moduleName} FAILED: no error."
        failures="${failures} ${moduleName}"
        fail=`expr ${fail} + 1`
    else
        echo "Test ${moduleName} passed."
        pass=`expr ${pass} + 1`
    fi
done

echo ""
echo "**** RESULTS"
echo ""
echo "${pass} Pass, ${fail} Fail: ${failures}"
