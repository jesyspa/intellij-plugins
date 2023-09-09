package com.intellij.plugins.serialmonitor.ui

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Key
import com.intellij.openapi.wm.ToolWindow
import com.intellij.plugins.serialmonitor.SerialPortProfile
import com.intellij.plugins.serialmonitor.service.SerialPortService
import com.intellij.plugins.serialmonitor.service.SerialPortsListener
import com.intellij.plugins.serialmonitor.service.SerialPortsListener.Companion.SERIAL_PORTS_TOPIC
import com.intellij.ui.JBSplitter
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.Align.Companion.FILL
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.application
import org.jetbrains.annotations.Nls
import javax.swing.JPanel
import javax.swing.SwingUtilities


private val SERIAL_PROFILE = Key<SerialPortProfile>(SerialPortProfile::javaClass.name)
private val SERIAL_MONITOR = Key<SerialMonitor>(SerialMonitor::javaClass.name)

class ConnectPanel(private val toolWindow: ToolWindow) : JBSplitter(false, 0.4f, 0.1f, 0.9f) {

  private val profileSubPanel = ProfileSubPanel(this)

  private val ports = ConnectableList(this)
  private val listToolbar = ActionManager.getInstance()
    .createActionToolbar(ActionPlaces.TOOLBAR, ports.toolbarActions, true)
    .apply {
      targetComponent = ports
    }

  init {
    firstComponent = portsPanel()
    //secondComponent = profilesSubPanel.component
    application.messageBus.connect(toolWindow.disposable)
      .subscribe(SERIAL_PORTS_TOPIC, object : SerialPortsListener {
        override fun portsStatusChanged() {
          updateLists()
        }
      })
  }

  private fun updateLists() {
    SwingUtilities.invokeLater {
      with(service<SerialPortService>()) {
        ports.rescanPorts()
      }
    }
  }

  private fun portsPanel(): JPanel =
    panel {
      row {
        cell(listToolbar.component)
      }
      row { scrollCell(ports).align(FILL) }.resizableRow()
      updateLists()
    }

  fun openConsole(portName: String?) {
    if (portName != null) {
      val contentManager = toolWindow.getContentManager()
      var content = contentManager.contents.firstOrNull { it.getUserData(SERIAL_PROFILE)?.portName == portName }
      if (content != null) {
        contentManager.setSelectedContent(content, true, true)
      }
    }
  }

  fun disconnectPort(portName: String?) {
    if (portName != null) {
      val contentManager = toolWindow.getContentManager()
      contentManager
        .contents
        .firstOrNull { it.getUserData(SERIAL_PROFILE)?.portName == portName }
        ?.getUserData(SERIAL_MONITOR)
        ?.disconnect()
    }
  }


  fun connectProfile(profile: SerialPortProfile,
                     name: @Nls String = profile.defaultName()) {
    val contentManager = toolWindow.getContentManager()
    val panel = SimpleToolWindowPanel(false, true)
    var content = ContentFactory.getInstance().createContent(panel, name, true)
    content.putUserData(ToolWindow.SHOW_CONTENT_ICON, true)
    content.putUserData(SERIAL_PROFILE, profile)
    val serialMonitor = SerialMonitor(toolWindow.getProject(), name, profile)
    content.putUserData(SERIAL_MONITOR, serialMonitor)
    panel.setContent(serialMonitor.component)
    content.setDisposer(serialMonitor)
    content.setCloseable(true)
    contentManager.addContent(content)
    val handler = object : SerialPortsListener {
      override fun portsStatusChanged() {
        SwingUtilities.invokeLater {
          content.icon = serialMonitor.status.statusIcon
        }
      }
    }
    application.messageBus.connect(content).subscribe<SerialPortsListener>(SERIAL_PORTS_TOPIC, handler)

    serialMonitor.connect()
    contentManager.setSelectedContent(content, true)
    toolWindow.setAvailable(true)
    toolWindow.show()
    toolWindow.activate(null, true)
  }

}