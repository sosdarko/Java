SET DEFINE ON
REM =======================================================
REM ========== Change below parameters as needed ==========
DEFINE fin_id=''
DEFINE release_number=''
DEFINE release_description=''
REM ========== Do not modify code below ==========
REM ==============================================

SET CONCAT ~

define node = &node

SPOOL main_&release_number~_&node.log

prompt
prompt MM1 release &release_number on &node
prompt

define mmdeployment_pwd = &mmdeployment_pwd

prompt
prompt connecting mmdeployment
connect mmdeployment/&mmdeployment_pwd@&node

set appinfo on;
exec mmRelUtil.createrelease('&fin_id',null,nvl('&release_description','&release_number'));
COMMIT;

select to_char(sysdate,'dd.mm.rrrr hh24:mi:ss') as "Release work start :" from dual;
SET TIMING ON
REM ==========================================================================
REM ========== Release work start - release content below this line ==========

REM release section

REM ========== Release work stop - release content finished - do not modify code below ==========
REM =============================================================================================
SET TIMING OFF
prompt
select to_char(sysdate,'dd.mm.rrrr hh24:mi:ss') as "Release work finished :" from dual;

exec mmRelUtil.FinishCreateRelease;
COMMIT;
SHOW ERRORS
DISCONNECT

prompt .........................................code signing.....................................................
disconnect
connect mmdeployment/&mmdeployment_pwd@&node 
set serverout on
set verify    off
begin 
  execute immediate 'begin codeMgr.CMMODULEMANAGER.CODESIGNING (''&release_number''); end;';
  commit;
  exception when others then dbms_output.put_line('...Code signing is not on yet!...');
end;
/

undef node
undef mmdeployment_pwd
undef fin_id
undef release_number
undef release_description

SPOOL OFF;
