package io.github.iur.arm.demo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.iur.arm.R
import io.github.iur.arm.fragment.fragmentation.ArmFragment

class BFragment : ArmFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.tvTitle).text = "B"
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        Log.d("DemoVisible", "B onSupportVisible")
    }

    override fun onSupportInvisible() {
        super.onSupportInvisible()
        Log.d("DemoVisible", "B onSupportInvisible")
    }
}

