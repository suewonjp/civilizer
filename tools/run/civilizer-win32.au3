#pragma compile(AutoItExecuteAllowed, True)

#include <Constants.au3>
#include <Process.au3>

_RunDos("java -version || (echo. >JreNotFound)")
If FileExists("JreNotFound") Then
    FileDelete("JreNotFound")
    MsgBox($MB_SYSTEMMODAL, "civilizer-win32.exe ERROR", "Can't find JRE (Java Runtime Environment)!" & @CRLF & "Download and install JRE from Oracle unless you haven't")
    Exit(1)
EndIf

Run("run-civilizer.bat", "", @SW_HIDE)
