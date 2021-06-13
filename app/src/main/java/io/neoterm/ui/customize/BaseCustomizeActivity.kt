package io.neoterm.ui.customize

import android.annotation.SuppressLint
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import io.neoterm.R
import io.neoterm.backend.TerminalSession
import io.neoterm.frontend.config.NeoTermPath
import io.neoterm.frontend.session.shell.ShellParameter
import io.neoterm.frontend.session.shell.client.BasicSessionCallback
import io.neoterm.frontend.session.shell.client.BasicViewClient
import io.neoterm.frontend.terminal.TerminalView
import io.neoterm.frontend.terminal.extrakey.ExtraKeysView
import io.neoterm.utils.Terminals

/**
 * @author kiva
 */
@SuppressLint("Registered")
open class BaseCustomizeActivity : AppCompatActivity() {
  lateinit var terminalView: TerminalView
  lateinit var viewClient: BasicViewClient
  lateinit var sessionCallback: BasicSessionCallback
  lateinit var session: TerminalSession
  lateinit var extraKeysView: ExtraKeysView

  fun initCustomizationComponent(layoutId: Int) {
    setContentView(layoutId)

    val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    terminalView = findViewById(R.id.terminal_view)
    extraKeysView = findViewById(R.id.custom_extra_keys)
    viewClient = BasicViewClient(terminalView)
    sessionCallback = BasicSessionCallback(terminalView)
    Terminals.setupTerminalView(terminalView, viewClient)
    Terminals.setupExtraKeysView(extraKeysView)

    val script = resources.getStringArray(R.array.custom_preview_script_colors)
    val parameter = ShellParameter()
      .executablePath("${NeoTermPath.USR_PATH}/bin/echo")
      .arguments(arrayOf("echo", "-e", *script))
      .callback(sessionCallback)
      .systemShell(false)

    session = Terminals.createSession(this, parameter)
    terminalView.attachSession(session)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      android.R.id.home -> finish()
    }
    return super.onOptionsItemSelected(item)
  }
}