package technology.cariad.entertainment.metadataservice.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class TabAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val fragments = mutableListOf<Pair<String, Fragment>>()

    override fun getItem(position: Int): Fragment {
        return fragments[position].second
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return fragments[position].first
    }

    /**
     * Adds a title with the specify title
     * @param fragment Fragment to add
     * @param title Title of the new fragment
     */
    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(title to fragment)
        notifyDataSetChanged()
    }
}

