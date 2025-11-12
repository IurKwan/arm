package io.github.iur.arm.demo

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.github.iur.arm.R
import io.github.iur.arm.fragment.fragmentation.ArmActivity

class FragmentDemoActivity : ArmActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_demo)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter =
            object : FragmentStateAdapter(this) {
                override fun getItemCount(): Int = 2

                override fun createFragment(position: Int): Fragment = if (position == 0) AFragment() else BFragment()
            }
    }
}
