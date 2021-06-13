package hilled.pwnterm.frontend.floating

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.appcompat.app.AlertDialog
import hilled.pwnterm.R
import hilled.pwnterm.backend.TerminalSession
import hilled.pwnterm.component.session.ShellParameter
import hilled.pwnterm.component.session.ShellTermSession
import hilled.pwnterm.frontend.session.terminal.BasicSessionCallback
import hilled.pwnterm.frontend.session.terminal.BasicViewClient
import hilled.pwnterm.frontend.session.view.TerminalView
import hilled.pwnterm.frontend.session.view.TerminalViewClient
import hilled.pwnterm.utils.Terminals

typealias DialogSessionFinished = (TerminalDialog, TerminalSession?) -> Unit

class TerminalDialog(val context: Context) {
  private val termWindowView = WindowTermView(context)
  private val terminalSessionCallback: BasicSessionCallback
  private var dialog: AlertDialog? = null
  private var terminalSession: TerminalSession? = null
  private var sessionFinishedCallback: DialogSessionFinished? = null
  private var cancelListener: DialogInterface.OnCancelListener? = null

  init {
    termWindowView.setTerminalViewClient(BasicViewClient(termWindowView.terminalView))
    terminalSessionCallback = object : BasicSessionCallback(termWindowView.terminalView) {
      override fun onSessionFinished(finishedSession: TerminalSession?) {
        sessionFinishedCallback?.let { it(this@TerminalDialog, finishedSession) }
        super.onSessionFinished(finishedSession)
      }
    }
  }

  fun execute(executablePath: String, arguments: Array<String>?): TerminalDialog {
    if (terminalSession != null) {
      terminalSession?.finishIfRunning()
    }

      dialog = MaterialAlertDialogBuilder(context)
      .setView(termWindowView.rootView)
      .setOnCancelListener {
        terminalSession?.finishIfRunning()
        cancelListener?.onCancel(it)
      }
      .create()

    val parameter = ShellParameter()
      .executablePath(executablePath)
      .arguments(arguments)
      .callback(terminalSessionCallback)
      .systemShell(false)
    terminalSession = Terminals.createSession(context, parameter)
    if (terminalSession is ShellTermSession) {
      (terminalSession as ShellTermSession).exitPrompt = context.getString(R.string.process_exit_prompt_press_back)
    }
    termWindowView.attachSession(terminalSession)
    return this
  }

  fun setTitle(title: String?): TerminalDialog {
    dialog?.setTitle(title)
    return this
  }

  fun onFinish(finishedCallback: DialogSessionFinished): TerminalDialog {
    this.sessionFinishedCallback = finishedCallback
    return this
  }

  fun show(title: String?) {
    dialog?.setTitle(title)
    dialog?.setCanceledOnTouchOutside(false)
    dialog?.show()
  }

  fun dismiss(): TerminalDialog {
    dialog?.dismiss()
    return this
  }

  fun imeEnabled(enabled: Boolean): TerminalDialog {
    if (enabled) {
      termWindowView.setInputMethodEnabled(true)
    }
    return this
  }
}

class WindowTermView(val context: Context) {
  @SuppressLint("InflateParams")
  var rootView: View = LayoutInflater.from(context).inflate(R.layout.ui_term_dialog, null, false)
    private set
  var terminalView: TerminalView = rootView.findViewById(R.id.terminal_view_dialog)
    private set

  init {
    Terminals.setupTerminalView(terminalView)
  }

  fun setTerminalViewClient(terminalViewClient: TerminalViewClient?) {
    terminalView.setTerminalViewClient(terminalViewClient)
  }

  fun attachSession(terminalSession: TerminalSession?) {
    terminalView.attachSession(terminalSession)
  }

  fun setInputMethodEnabled(enabled: Boolean) {
    terminalView.isFocusable = enabled
    terminalView.isFocusableInTouchMode = enabled
  }
}
