package com.alexwan.csci310team36projectd;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class RecyclerViewMatcher {
    private final int recyclerViewId;

    public RecyclerViewMatcher(int recyclerViewId) {
        this.recyclerViewId = recyclerViewId;
    }

    public Matcher<View> atPosition(final int position) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("matches item at position " + position);
            }

            @Override
            protected boolean matchesSafely(View view) {
                RecyclerView recyclerView = view.getRootView().findViewById(recyclerViewId);
                if (recyclerView == null || recyclerView.getAdapter() == null) {
                    return false;
                }

                RecyclerView.ViewHolder holder =
                        recyclerView.findViewHolderForAdapterPosition(position);
                return holder != null && holder.itemView == view;
            }
        };
    }
}