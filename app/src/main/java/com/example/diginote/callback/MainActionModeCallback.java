package com.example.diginote.callback;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.example.diginote.R;
import com.example.diginote.adapter.TodoAdapter;

public class MainActionModeCallback implements ActionMode.Callback{
    private ActionMode action;
    private MenuItem countItem;
    private MenuItem shareItem;
    TodoAdapter adapter;

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);
        this.action = actionMode;
        this.countItem = menu.findItem(R.id.action_checked_count);
        this.shareItem = menu.findItem(R.id.action_share_note);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mode = null;


    }

    public void setCount(String chackedCount) {
        if (countItem != null)
            this.countItem.setTitle(chackedCount);
    }
    /**
     * if checked count > 1 hide shareItem else show it
     *
     * @param b :visible
     */
    public void changeShareItemVisible(boolean b) {
        shareItem.setVisible(b);
    }

    public ActionMode getAction() {
        return action;
    }

/*
    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked){
            System.out.println("Checked");

        }
        else
        {
            System.out.println("not checked");

            adapter.setMultiCheckMode(false);
            adapter.setListener((NoteListener) this);


        }

    }*/
}
