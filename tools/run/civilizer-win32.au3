#pragma compile(AutoItExecuteAllowed, True)

#include <Constants.au3>

Local $pid = Run(@ComSpec & " /c " & 'where java', "", @SW_HIDE)  
If $pid == 0 Then
    MsgBox($MB_SYSTEMMODAL, "civilizer-win32.exe ERROR", "Can't find JRE (Java Runtime Environment)!" & chr(13) & chr(10) & "Download and install JRE from Oracle unless you haven't")
EndIf

Run("run-civilizer.bat", "", @SW_HIDE)
