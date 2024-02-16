package com.android.signlanguagetranslator.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.viewpager.widget.ViewPager
import com.android.signlanguagetranslator.MainViewModel
import com.android.signlanguagetranslator.R
import com.android.signlanguagetranslator.ViewPagerAdapter
import com.android.signlanguagetranslator.databinding.FragmentInstructionBinding


class InstructionFragment: Fragment() {

    private var _fragmentInstructionBinding: FragmentInstructionBinding? = null

    private val fragmentInstructionBinding
        get() = _fragmentInstructionBinding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var slideViewPager: ViewPager
    private lateinit var dotLayout: LinearLayout
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentInstructionBinding =
            FragmentInstructionBinding.inflate(inflater, container, false)

        return fragmentInstructionBinding.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*viewModel.currentStartup.observe(viewLifecycleOwner){
            if (it) findNavController().navigate(R.id.action_instruction_to_permissions)
        }*/

        slideViewPager = fragmentInstructionBinding.slideViewPager
        dotLayout = fragmentInstructionBinding.indicatorLayout

        viewPagerAdapter = ViewPagerAdapter(requireContext());

        slideViewPager.setAdapter(viewPagerAdapter);

        setUpindicator(0);
        slideViewPager.addOnPageChangeListener(viewListener);
/*        Navigation.findNavController(
            requireActivity(), R.id.fragment_container
        ).navigate(R.id.instruction_fragment)*/

        fragmentInstructionBinding.backbtn.setOnClickListener{
            if (getitem(0) > 0){
                slideViewPager.setCurrentItem(getitem(-1),true);
            }
        }
        fragmentInstructionBinding.nextbtn.setOnClickListener{
            if (getitem(0) < 3) slideViewPager.setCurrentItem(getitem(1), true) else {
                viewModel.setStartup(true)
                Navigation.findNavController(
                    requireActivity(), R.id.fragment_container
                ).navigate(R.id.action_instruction_to_permissions)
            }
        }
    }

    fun setUpindicator(position: Int) {
        val dots: Array<TextView?> = arrayOfNulls(4)
        dotLayout.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(requireContext())
            dots[i]?.setText(Html.fromHtml("&#8226"))
            dots[i]?.setTextSize(35F)
            dots[i]
                ?.setTextColor(getResources().getColor(R.color.mp_bounding_box))
            dotLayout.addView(dots[i])
        }
        dots.get(position)
            ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.mp_color_primary_dark))
    }

    var viewListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            setUpindicator(position)
            if (position > 0) {
                fragmentInstructionBinding.backbtn.visibility = View.VISIBLE
            } else {
                fragmentInstructionBinding.backbtn.visibility = View.INVISIBLE
            }
        }

        override fun onPageScrollStateChanged(state: Int) {}
    }

    private fun getitem(i: Int): Int {
        return slideViewPager.currentItem + i
    }
}