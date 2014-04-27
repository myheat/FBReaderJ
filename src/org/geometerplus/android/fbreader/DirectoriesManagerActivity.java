/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader;

import android.os.Bundle;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.app.Activity;

import java.util.ArrayList;
import java.lang.Runnable;

import android.content.Context;
import android.content.Intent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.widget.*;
import android.view.*;

import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.android.util.FileChooserUtil;

import org.geometerplus.zlibrary.ui.android.R;

public class DirectoriesManagerActivity extends Activity{
	private final int ADD_NEW_DIR_POSITION = 0;
    public static final String TITLE = "dir_manager_title";
    public static final String CHOOSER_TITLE = "chooser_title";
    public static final String DIR_LIST = "dir_list";
    public static final String WRITABLE_DIRS_ONLY = "writable_dirs_only";
	
	private DirectoriesAdapter myAdapter;
	private ListView myListView;
	private String myDefaultDir = "/";
	private ArrayList<String> myDirList;
	private String myChooserTitle;
	private String myTitle;
	private int myAddNewDirPosition = 0;
	private ZLResource myResource;
	private boolean myChooseWritableDirectoriesOnly;

	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dir_manager);
		
		myTitle = getIntent().getStringExtra(TITLE);
		myChooserTitle = getIntent().getStringExtra(CHOOSER_TITLE);
		myDirList = getIntent().getStringArrayListExtra(DIR_LIST);
		myChooseWritableDirectoriesOnly = getIntent().getBooleanExtra(WRITABLE_DIRS_ONLY, true);

		setTitle(myTitle);
		
		myResource = ZLResource.resource("dialog").getResource("dirManager");
		
        setupActionButtons();
		myListView = (ListView) findViewById(R.id.directories);

		myDirList.add(ADD_NEW_DIR_POSITION, myResource.getResource("addNewDirButton").getValue());
		setupDirectoriesAdapter(myDirList);
	}
	
	private void openFileChooser(int index, String dirName){
		FileChooserUtil.runDirectoryChooser(
						this,
						index,
						myChooserTitle,
						dirName,
						myChooseWritableDirectoriesOnly
					);
	}
	
	private void showMessage(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	private void updateDirs(int index, Intent data){
		String path = FileChooserUtil.pathFromData(data);
		if(!myDirList.contains(path)){
			myDirList.remove(index);
			myDirList.add(index, path);
			myAdapter.notifyDataSetChanged();
		}else{
			showMessage("Cannot add the duplicate directory "+path);
		}
	}	

	private void addNewDir(Intent data){
		String path = FileChooserUtil.pathFromData(data);
		if(!myDirList.contains(path)){
			myDirList.add(FileChooserUtil.pathFromData(data));
			myAdapter.notifyDataSetChanged();
		}else{
			showMessage("Cannot add the duplicate directory "+path);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
			if(requestCode != ADD_NEW_DIR_POSITION){
				updateDirs(requestCode, data);
			}else{
				addNewDir(data);
			}
		}
	}
	
	private void setupDirectoriesAdapter(ArrayList<String> dirs){
		myAdapter = new DirectoriesAdapter(this, dirs);
		myListView.setAdapter(myAdapter);
		myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
					String dirName = (String) parent.getItemAtPosition(position);
					if(position <= 0){
						dirName = myDefaultDir;
					}
					openFileChooser(position, dirName);		
				}
		});
	}
    
    private void setupActionButtons(){
        final Button okButton = (Button) findViewById(R.id.button_ok);
        okButton.setText(myResource.getResource("ok").getValue());
		okButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
				myDirList.remove(0);
				Intent result = new Intent();
				result.putStringArrayListExtra(DIR_LIST, myDirList);
				setResult(RESULT_OK, result);
				finish();
             }
        });
        final Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setText(myResource.getResource("cancel").getValue());
        cancelButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
				setResult(RESULT_CANCELED);
                finish();
             }
         });
    }

	private class DirectoriesAdapter extends ArrayAdapter<String>{
		public DirectoriesAdapter(Context context, ArrayList<String> dirs){
			super(context, R.layout.dir_list, dirs);
		}
	
		@Override
		public View getView (final int position, View convertView, ViewGroup parent){
			
			final View view = LayoutInflater.from(getContext()).inflate(R.layout.dir_list, parent, false);
			
			final String dirName = (String) getItem(position);
			
			TextView title = (TextView) view.findViewById(R.id.title);
			title.setText(dirName);
			ImageView deleteButton = (ImageView) view.findViewById(R.id.delete);
			
			if(position != ADD_NEW_DIR_POSITION){
				deleteButton.setVisibility(View.VISIBLE);
				deleteButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						view.animate()
							.setDuration(300)
							.alpha(0)
							.setListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									myDirList.remove(position);
									myAdapter.notifyDataSetChanged();
									view.setAlpha(1);
								}
							});
					}
				});
			}else{	
				deleteButton.setVisibility(View.INVISIBLE);
			}
	
			return view;
		}
	}
}
