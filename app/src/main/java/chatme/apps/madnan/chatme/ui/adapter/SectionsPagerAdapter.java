package chatme.apps.madnan.chatme.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import chatme.apps.madnan.chatme.R;
import chatme.apps.madnan.chatme.ui.fragments.ChatsFragment;
import chatme.apps.madnan.chatme.ui.fragments.FriendsFragment;
import chatme.apps.madnan.chatme.ui.fragments.RequestsFragment;

/**
 * Created by mahmoud adnan on 10/29/2017.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){

            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;


            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return "CHATS";
            case 1:
                return "REQUESTS";
            case 2:
                return "FRIENDS";
            default:
                return null;
        }
    }
}
