include "aiBrowser.jsh"

Script JawsOn ()
  let g_aiBrowserFlag = 1
EndScript

Script JawsOff ()
  let g_aiBrowserFlag = 0
EndScript

;--------------------------------------------------------------------------------
Script AiBrowserSayAllOff ()
  If g_aiBrowserSayAllFlag Then
    let g_aiBrowserSayAllFlag = 0
    StopSpeech()
    Delay(5, 1)
  EndIf
EndScript

Void Function FocusChangedEvent(Handle hCurWin, Handle hPrevWin)
If g_aiBrowserSayAllFlag Then
    return
  Else
    FocusChangedEvent(hCurWin, hPrevWin)
  EndIf
EndFunction

void function FocusChangedEventEx(Handle hwndFocus, int nObject, int nChild,
                                  Handle hwndPrevFocus, int nPrevObject, int nPrevChild,
	                          int nChangeDepth)
  If g_aiBrowserSayAllFlag Then
    return
  Else
    FocusChangedEventEx(hwndFocus, nObject, nChild, hwndPrevFocus, nPrevObject, nPrevChild, nChangeDepth)
  EndIf
EndFunction

int function FocusRedirected(Handle focusWindow, Handle prevWindow)
  If g_aiBrowserSayAllFlag Then
    return 1
  Else
    FocusRedirected(focusWindow, prevWindow)
  EndIf
EndFunction

Void Function SayTutorialHelp (int iObjType, int IsScriptKey)
  If g_aiBrowserSayAllFlag Then
    return
  Else
    SayTutorialHelp(iObjType, IsScriptKey)
  EndIf
EndFunction

Void Function SendaiBrowserMessage (Int param)
  SendMessage(g_aiBrowserSayAllWindow, g_aiBrowserSayAllMessage, param, 0);
EndFunction

Void Function SayAllStoppedEvent ()
  PCCursor()
  If (!SayAllInProgress()) Then
    SendaiBrowserMessage(0)
    PCCursor()
  EndIf
EndFunction

Script JAWSCursor ()
EndScript

Script InvisibleCursor ()
EndScript

Void Function ObserveSpeechFunction ()
    If (SayAllInProgress()) Then
    Else
      If g_aiBrowserSayAllFlag != 1 Then
        let g_aiBrowserSayAllWindow = FindWindow(GetAppMainWindow(GetFocus()), "Jaws-aiBrowser-Communication", "")
        ;SayInteger(g_aiBrowserSayAllWindow)
        ;SetActiveCursor(1)
        ;MoveToWindow(g_aiBrowserSayAllWindow)
        let g_aiBrowserSayAllMessage = RegisterWindowMessage("WM_JAWS_AIBROWSER_MESSAGE")
        let g_aiBrowserSayAllFlag = 1
        PCCursor()
        SetFocus(g_aiBrowserSayAllWindow)
        PCCursor()
        Delay(5, 0)
      Else
        If (GetFocus() != g_aiBrowserSayAllWindow) Then
          SetFocus(g_aiBrowserSayAllWindow)
        EndIf
        PCCursor()
      EndIf

      PCCursor()
      SayAll(0)
      ;SkimRead()
      PCCursor()
    EndIf
EndFunction

Script ObserveSpeech ()
  ObserveSpeechFunction()
EndScript
;--------------------------------------------------------------------------------


Script func_Alt()
  If g_aiBrowserFlag Then
    SayString("Alt")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt")
EndScript


Script func_Control_N()
  If g_aiBrowserFlag Then
    SayString("Control+N")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+N")
EndScript


Script func_Control_Q()
  If g_aiBrowserFlag Then
    SayString("Control+Q")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+Q")
EndScript


Script func_Control()
  If g_aiBrowserFlag Then
    SayString("Control")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control")
EndScript


Script func_Control_Control()
  If g_aiBrowserFlag Then
    SayString("Control+Control")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control")
EndScript


Script func_Shift_Shift()
  If g_aiBrowserFlag Then
    SayString("Shift+Shift")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Shift")
EndScript


Script func_Control_RightArrow()
  If g_aiBrowserFlag Then
    PerformScript SayNextWord()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Control+RightArrow")
  EndIf
EndScript


Script func_Control_LeftArrow()
  If g_aiBrowserFlag Then
    PerformScript SayPriorWord()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Control+LeftArrow")
  EndIf
EndScript


Script func_Control_UpArrow()
  If g_aiBrowserFlag Then
    PerformScript ControlUpArrow()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Control+UpArrow")
  EndIf
EndScript


Script func_Control_DownArrow()
  If g_aiBrowserFlag Then
    PerformScript ControlDownArrow()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Control+DownArrow")
  EndIf
EndScript


Script func_Control_O()
  If g_aiBrowserFlag Then
    SayString("Control+O")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+O")
EndScript


Script func_Alt_Control_I()
  If g_aiBrowserFlag Then
    SayString("Alt+Control+I")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+I")
EndScript


Script func_Control_I()
  If g_aiBrowserFlag Then
    SayString("Control+I")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+I")
EndScript


Script func_Control_W()
  If g_aiBrowserFlag Then
    SayString("Control+W")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+W")
EndScript


Script func_DownArrow()
  If g_aiBrowserFlag Then
    PerformScript SayNextLine()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("DownArrow")
  EndIf
EndScript


Script func_UpArrow()
  If g_aiBrowserFlag Then
    PerformScript SayPriorLine()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("UpArrow")
  EndIf
EndScript


Script func_Alt_DownArrow()
  If g_aiBrowserFlag Then
    PerformScript OpenListBox()
  Else
    If g_aiBrowserSayAllFlag Then
      return
    EndIf
    TypeKey("Alt+DownArrow")
  EndIf
EndScript


Script Enter()
  If g_aiBrowserFlag Then
    PerformScript Enter()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Enter()
  If g_aiBrowserFlag Then
    SayString("Enter")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Enter")
EndScript


Script VirtualSpacebar()
  If g_aiBrowserFlag Then
    PerformScript VirtualSpacebar()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Space()
  If g_aiBrowserFlag Then
    SayString("Space")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Space")
EndScript


Script func_Control_F()
  If g_aiBrowserFlag Then
    SayString("Control+F")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+F")
EndScript


Script func_Control_R()
  If g_aiBrowserFlag Then
    SayString("Control+R")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+R")
EndScript


Script func_Alt_Control_K()
  If g_aiBrowserFlag Then
    SayString("Alt+Control+K")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+K")
EndScript


Script func_F4()
  If g_aiBrowserFlag Then
    PerformScript UtilitySetFontMode()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("F4")
  EndIf
EndScript


Script func_Alt_Control_N()
  If g_aiBrowserFlag Then
    SayString("Alt+Control+N")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+N")
EndScript


Script func_Alt_Control_A()
  If g_aiBrowserFlag Then
    SayString("Alt+Control+A")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+A")
EndScript


Script func_Home()
  If g_aiBrowserFlag Then
    PerformScript JAWSHome()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Home")
  EndIf
EndScript


Script func_Control_Home()
  If g_aiBrowserFlag Then
    PerformScript TopOfFile()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Control+Home")
  EndIf
EndScript


Script func_End()
  If g_aiBrowserFlag Then
    PerformScript JAWSEnd()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("End")
  EndIf
EndScript


Script func_Control_End()
  If g_aiBrowserFlag Then
    PerformScript BottomOfFile()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Control+End")
  EndIf
EndScript


Script func_RightArrow()
  If g_aiBrowserFlag Then
    PerformScript SayNextCharacter()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("RightArrow")
  EndIf
EndScript


Script moveToNextHeading()
  If g_aiBrowserFlag Then
    PerformScript moveToNextHeading()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_H()
  If g_aiBrowserFlag Then
    SayString("H")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("H")
EndScript


Script func_Tab()
  If g_aiBrowserFlag Then
    PerformScript Tab()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Tab")
  EndIf
EndScript


Script MoveToNextObject()
  If g_aiBrowserFlag Then
    PerformScript MoveToNextObject()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_O()
  If g_aiBrowserFlag Then
    SayString("O")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("O")
EndScript


Script FocusToNextField()
  If g_aiBrowserFlag Then
    PerformScript FocusToNextField()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_F()
  If g_aiBrowserFlag Then
    SayString("F")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("F")
EndScript


Script MoveToNextListItem()
  If g_aiBrowserFlag Then
    PerformScript MoveToNextListItem()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_I()
  If g_aiBrowserFlag Then
    SayString("I")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("I")
EndScript


Script MoveToNextNonLinkText()
  If g_aiBrowserFlag Then
    PerformScript MoveToNextNonLinkText()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_N()
  If g_aiBrowserFlag Then
    SayString("N")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("N")
EndScript


Script MoveToNextFrame()
  If g_aiBrowserFlag Then
    PerformScript MoveToNextFrame()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_M()
  If g_aiBrowserFlag Then
    SayString("M")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("M")
EndScript


Script MoveToNextAnchor()
  If g_aiBrowserFlag Then
    PerformScript MoveToNextAnchor()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_A()
  If g_aiBrowserFlag Then
    SayString("A")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("A")
EndScript


Script func_LeftArrow()
  If g_aiBrowserFlag Then
    PerformScript SayPriorCharacter()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("LeftArrow")
  EndIf
EndScript


Script moveToPriorHeading()
  If g_aiBrowserFlag Then
    PerformScript moveToPriorHeading()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Shift_H()
  If g_aiBrowserFlag Then
    SayString("Shift+H")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Shift+H")
EndScript


Script func_Shift_Tab()
  If g_aiBrowserFlag Then
    PerformScript ShiftTab()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Shift+Tab")
  EndIf
EndScript


Script MoveToPriorObject()
  If g_aiBrowserFlag Then
    PerformScript MoveToPriorObject()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Shift_O()
  If g_aiBrowserFlag Then
    SayString("Shift+O")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Shift+O")
EndScript


Script FocusToPriorField()
  If g_aiBrowserFlag Then
    PerformScript FocusToPriorField()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Shift_F()
  If g_aiBrowserFlag Then
    SayString("Shift+F")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Shift+F")
EndScript


Script MoveToPriorListItem()
  If g_aiBrowserFlag Then
    PerformScript MoveToPriorListItem()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Shift_I()
  If g_aiBrowserFlag Then
    SayString("Shift+I")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Shift+I")
EndScript


Script MoveToPriorNonLinkText()
  If g_aiBrowserFlag Then
    PerformScript MoveToPriorNonLinkText()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Shift_N()
  If g_aiBrowserFlag Then
    SayString("Shift+N")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Shift+N")
EndScript


Script MoveToPriorFrame()
  If g_aiBrowserFlag Then
    PerformScript MoveToPriorFrame()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Shift_M()
  If g_aiBrowserFlag Then
    SayString("Shift+M")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Shift+M")
EndScript


Script MoveToPriorAnchor()
  If g_aiBrowserFlag Then
    PerformScript MoveToPriorAnchor()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Shift_A()
  If g_aiBrowserFlag Then
    SayString("Shift+A")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Shift+A")
EndScript


Script MoveToNextHeadingLevelN(int n)
  If g_aiBrowserFlag Then
    PerformScript MoveToNextHeadingLevelN(n)
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_1()
  If g_aiBrowserFlag Then
    SayString("1")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_2()
  If g_aiBrowserFlag Then
    SayString("2")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_3()
  If g_aiBrowserFlag Then
    SayString("3")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_4()
  If g_aiBrowserFlag Then
    SayString("4")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_5()
  If g_aiBrowserFlag Then
    SayString("5")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_6()
  If g_aiBrowserFlag Then
    SayString("6")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script MoveToPriorHeadingLevelN(int n)
  If g_aiBrowserFlag Then
    PerformScript MoveToPriorHeadingLevelN(n)
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Shift_1()
  If g_aiBrowserFlag Then
    SayString("Shift+1")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_Shift_2()
  If g_aiBrowserFlag Then
    SayString("Shift+2")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_Shift_3()
  If g_aiBrowserFlag Then
    SayString("Shift+3")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_Shift_4()
  If g_aiBrowserFlag Then
    SayString("Shift+4")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_Shift_5()
  If g_aiBrowserFlag Then
    SayString("Shift+5")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_Shift_6()
  If g_aiBrowserFlag Then
    SayString("Shift+6")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeCurrentScriptKey()
EndScript


Script func_Control_P()
  If g_aiBrowserFlag Then
    SayString("Control+P")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+P")
EndScript


Script func_Control_S()
  If g_aiBrowserFlag Then
    SayString("Control+S")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+S")
EndScript


Script func_Pause()
  If g_aiBrowserFlag Then
    SayString("Pause")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+Shift+P")
EndScript


Script func_Control_Pause()
  If g_aiBrowserFlag Then
    SayString("Control+Pause")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+Shift+P")
EndScript


Script func_Alt_Control_Shift_P()
  If g_aiBrowserFlag Then
    SayString("Alt+Control+Shift+P")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+Shift+P")
EndScript


Script func_Control_M()
  If g_aiBrowserFlag Then
    SayString("Control+M")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+M")
EndScript


Script JumpToTableCell()
  If g_aiBrowserFlag Then
    PerformScript JumpToTableCell()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Control_J()
  If g_aiBrowserFlag Then
    SayString("Control+J")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+J")
EndScript


Script DefineATempPlaceMarker()
  If g_aiBrowserFlag Then
    PerformScript DefineATempPlaceMarker()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Control_K()
  If g_aiBrowserFlag Then
    SayString("Control+K")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+K")
EndScript


Script JumpReturnFromTableCell()
  If g_aiBrowserFlag Then
    PerformScript JumpReturnFromTableCell()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Control_Shift_J()
  If g_aiBrowserFlag Then
    SayString("Control+Shift+J")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+Shift+J")
EndScript


Script SelectAPlaceMarker()
  If g_aiBrowserFlag Then
    PerformScript SelectAPlaceMarker()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_Control_Shift_K()
  If g_aiBrowserFlag Then
    SayString("Control+Shift+K")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+Shift+K")
EndScript


Script func_Alt_Control_PageUp()
    PerformScript IncreaseVoiceRate()
EndScript


Script func_Alt_Control_PageDown()
    PerformScript DecreaseVoiceRate()
EndScript


Script func_Control_A()
  If g_aiBrowserFlag Then
    PerformScript SelectAll()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Control+A")
  EndIf
EndScript


Script func_Control_H()
  If g_aiBrowserFlag Then
    SayString("Control+H")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Control+H")
EndScript


Script func_Alt_Control_S()
  If g_aiBrowserFlag Then
    SayString("Alt+Control+S")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+S")
EndScript


Script func_Alt_Control_R()
  If g_aiBrowserFlag Then
    SayString("Alt+Control+R")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+R")
EndScript


Script func_F5()
  If g_aiBrowserFlag Then
    PerformScript UtilityInitializeHomeRowPosition()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("F5")
  EndIf
EndScript


Script func_Alt_RightArrow()
  If g_aiBrowserFlag Then
    SayString("Alt+RightArrow")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+RightArrow")
EndScript


Script func_Alt_LeftArrow()
  If g_aiBrowserFlag Then
    SayString("Alt+LeftArrow")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+LeftArrow")
EndScript


Script func_Alt_D()
  If g_aiBrowserFlag Then
    SayString("Alt+D")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+D")
EndScript


Script func_Control_Tab()
  If g_aiBrowserFlag Then
    PerformScript NextDocumentWindow()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Control+Tab")
  EndIf
EndScript


Script func_Control_Shift_Tab()
  If g_aiBrowserFlag Then
    PerformScript PreviousDocumentWindow()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Control+Shift+Tab")
  EndIf
EndScript


Script MoveToNextSameElement()
  If g_aiBrowserFlag Then
    PerformScript MoveToNextSameElement()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_S()
  If g_aiBrowserFlag Then
    SayString("S")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("S")
EndScript


Script MoveToNextTable()
  If g_aiBrowserFlag Then
    PerformScript MoveToNextTable()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeCurrentScriptKey()
  EndIf
EndScript


Script func_T()
  If g_aiBrowserFlag Then
    SayString("T")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("T")
EndScript


Script func_Alt_Control_T()
  If g_aiBrowserFlag Then
    SayString("Alt+Control+T")
  EndIf
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
  TypeKey("Alt+Control+T")
EndScript


Script func_Backspace()
  If g_aiBrowserFlag Then
    PerformScript JAWSBackspace()
  Else
    If g_aiBrowserSayAllFlag Then
      PerformScript AiBrowserSayAllOff()
      SendaiBrowserMessage(1)
      Delay(5, 1)
    EndIf
    TypeKey("Backspace")
  EndIf
EndScript


