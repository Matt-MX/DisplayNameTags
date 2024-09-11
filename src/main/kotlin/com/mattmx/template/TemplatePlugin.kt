package com.mattmx.template

import com.mattmx.ktgui.GuiManager
import org.bukkit.plugin.java.JavaPlugin

class TemplatePlugin : JavaPlugin() {

    override fun onEnable() {
        instance = this
        GuiManager.init(this)
    }

    companion object {
        private lateinit var instance: TemplatePlugin
        fun get() = instance
    }

}