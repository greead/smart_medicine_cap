package com.example.seniordesign2;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayDeque;

public class FragmentStack extends ArrayDeque<Fragment> {

    FragmentManager fragmentManager;
    int containerID;

    public FragmentStack(FragmentManager fragmentManager, int containerID) {
        super();
        this.fragmentManager = fragmentManager;
        this.containerID = containerID;
    }

    public void setCurrent(Fragment fragment) {
        addFirst(fragment);
        fragmentManager.beginTransaction()
                .replace(containerID, fragment)
                .commit();
    }

    public void setPrevious() {
        if (size() > 1) {
            removeFirst();
            Fragment previous = getFirst();
            fragmentManager.beginTransaction()
                    .replace(containerID, previous)
                    .commit();
        }
    }

}
