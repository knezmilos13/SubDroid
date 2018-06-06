package knez.assdroid.editor;

import butterknife.BindView;
import butterknife.ButterKnife;
import knez.assdroid.App;
import knez.assdroid.common.mvp.CommonSubtitleActivity;
import knez.assdroid.common.mvp.CommonSubtitleMVP;
import knez.assdroid.translator.TranslatorActivity;
import knez.assdroid.R;
import knez.assdroid.common.adapter.IdentifiableAdapter;
import knez.assdroid.editor.adapter.SubtitleLineAdapterPack;
import knez.assdroid.editor.gui.SubtitleLineLayoutItem;
import knez.assdroid.editor.vso.SubtitleLineVso;
import knez.assdroid.util.gui.BgpSearchView;
import knez.assdroid.util.gui.DividerItemDecoration;
import knez.assdroid.util.gui.FadeAnimationHelper;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;

import java.util.ArrayList;
import java.util.List;

import static knez.assdroid.editor.EditorMVP.*;

public class EditorActivity extends CommonSubtitleActivity
        implements ViewInterface, SubtitleLineLayoutItem.Callback, BgpSearchView.Listener {

    private static final int REQUEST_CODE_OPEN_SUBTITLE = 1234;
    private static final int REQUEST_CODE_TRANSLATOR_ACTIVITY = 500;

    private static final long DRAWER_ID_SHOW_TIMING = 1;
    private static final long DRAWER_ID_SHOW_ACTOR_STYLE = 2;
    private static final long DRAWER_ID_SIMPLIFY_TAGS = 3;

    @BindView(R.id.editor_subtitle_list) protected RecyclerView itemListRecycler;
    @BindView(R.id.editor_search_view_container) protected View searchViewContainer;
    @BindView(R.id.editor_search_view) protected BgpSearchView searchView;
    @BindView(R.id.editor_search_view_shadow) protected View searchViewShadow;
    @BindView(R.id.editor_center_text) protected TextView centerTextView;
    @BindView(R.id.toolbar) protected Toolbar toolbar;

    private PresenterInterface presenter;
    private IdentifiableAdapter subtitleLinesAdapter;
    private LinearLayoutManager linearLayoutManager;

    private Drawer drawer;
    private SwitchDrawerItem showTimingsDrawerItem;
    private SwitchDrawerItem showActorStyleDrawerItem;
    private SwitchDrawerItem simplifyTagsDrawerItem;


    // --------------------------------------------------------------------------- LIFECYCLE & SETUP

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        setUpInterface(savedInstanceState);

        Object retainedInstance = getLastCustomNonConfigurationInstance();
        if(retainedInstance != null && retainedInstance instanceof PresenterInterface) {
            presenter = (PresenterInterface) retainedInstance;
        } else {
            presenter = App.getAppComponent().getEditorPresenter();
        }

        presenter.onAttach(this);
	}

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return presenter;
    }

    @Override
    protected CommonSubtitleMVP.PresenterInterface getPresenter() {
        return presenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.setListener(this);
    }

    @Override
    protected void onPause() {
        searchView.setListener(null);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        presenter.onDetach();
        presenter = null;
        super.onDestroy();
    }

    private void setUpInterface(@Nullable Bundle savedInstanceState) {
	    setUpToolbar();
        setUpAdapter();
        setUpList();
        setUpDrawer(savedInstanceState);
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
    }

    private void setUpDrawer(@Nullable Bundle savedInstanceState) {
	    showTimingsDrawerItem = new SwitchDrawerItem()
                .withName(R.string.editor_drawer_show_timing)
                .withIdentifier(DRAWER_ID_SHOW_TIMING)
                .withChecked(true)
                .withSelectable(false)
                .withOnCheckedChangeListener(
                        (drawerItem, buttonView, isChecked) -> onShowTimingsCheckChanged(isChecked));

	    showActorStyleDrawerItem = new SwitchDrawerItem()
                .withName(R.string.editor_drawer_show_actor_style)
                .withIdentifier(DRAWER_ID_SHOW_ACTOR_STYLE)
                .withChecked(true)
                .withSelectable(false)
                .withOnCheckedChangeListener((drawerItem, buttonView, isChecked)
                        -> onShowActorStyleCheckChanged(isChecked));

	    simplifyTagsDrawerItem = new SwitchDrawerItem()
                .withName(R.string.editor_drawer_simplify_tags)
                .withIdentifier(DRAWER_ID_SIMPLIFY_TAGS)
                .withChecked(true)
                .withSelectable(false)
                .withOnCheckedChangeListener((drawerItem, buttonView, isChecked)
                        -> onSimplifyTagsCheckChanged(isChecked));

        drawer = new DrawerBuilder(this)
                .withRootView(R.id.drawer_container)
                .withToolbar(toolbar)
                .withDisplayBelowStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        new SectionDrawerItem()
                                .withName(R.string.editor_drawer_lines_display),
                        showTimingsDrawerItem,
                        showActorStyleDrawerItem,
                        simplifyTagsDrawerItem
                )
                .withSelectedItem(-1)
                .withSavedInstance(savedInstanceState)
                .build();
    }

    private void setUpAdapter() {
        subtitleLinesAdapter = new IdentifiableAdapter(this);
        SubtitleLineAdapterPack slap = new SubtitleLineAdapterPack(this);
        subtitleLinesAdapter.addAdapterPack(slap);
    }

    private void setUpList() {
        linearLayoutManager = new LinearLayoutManager(this);

        itemListRecycler.setLayoutManager(linearLayoutManager);
        itemListRecycler.setAdapter(subtitleLinesAdapter);
        itemListRecycler.addItemDecoration(
                new DividerItemDecoration(this, R.drawable.list_item_divider, false, false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_editor, menu);
        menu.getItem(0).setIcon(
                new IconDrawable(this, MaterialIcons.md_search).color(Color.WHITE).sizeDp(24));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = drawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }


	// --------------------------------------------------------------------------------- USER EVENTS

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen()) drawer.closeDrawer();
        else super.onBackPressed();
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    case R.id.menu_item_search:
                searchViewContainer.setVisibility(View.VISIBLE);
                searchViewShadow.setVisibility(View.VISIBLE);
		        break;
			case R.id.menu_item_create_subtitle:
			    presenter.onNewSubtitleRequested();
				break;
			case R.id.menu_item_load_subtitle:
				showFileOpenSelector();
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		return true;
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == REQUEST_CODE_OPEN_SUBTITLE) {
            if(resultCode != RESULT_OK) return;
            if(intent == null) return;

            Uri uri = intent.getData();
            if(uri == null) return;

            presenter.onFileSelectedForLoad(uri);
            return;
        } else if(requestCode == REQUEST_CODE_TRANSLATOR_ACTIVITY) {
            //noinspection unchecked
            ArrayList<Long> editedLineIds = (ArrayList<Long>)
                    intent.getSerializableExtra(TranslatorActivity.OUTPUT_EDITED_LINE_NUMBERS);
            if(editedLineIds.size() > 0) presenter.onSubtitleEditedExternally(editedLineIds);

            int lastViewedLineNumber =
                    intent.getIntExtra(TranslatorActivity.OUTPUT_LAST_VIEWED_LINE_NUMBER, 0);
            if(lastViewedLineNumber != 0)
                linearLayoutManager.scrollToPositionWithOffset(lastViewedLineNumber - 1, 0);
        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSubtitleLineClicked(@NonNull SubtitleLineVso subtitleLineVso,
                                      @NonNull SubtitleLineLayoutItem layoutItem) {
	    presenter.onSubtitleLineClicked(subtitleLineVso.getId());
    }

    @Override
    public void onXClicked() {
	    presenter.onEndSearchRequested();
    }
    @Override
    public void onSearchSubmitted(@NonNull String text) {
	    presenter.onSearchSubmitted(text);
    }
    @Override
    public void onLetterInputted(@NonNull String text) {
	    onSearchSubmitted(text);
    }
    @Override
    public void onPrevResultRequested() {
	    presenter.onPrevSearchResultRequested();
    }
    @Override
    public void onNextResultRequested() {
	    presenter.onNextSearchResultRequested();
    }

    private void onShowTimingsCheckChanged(boolean isChecked) {
        presenter.onShowTimingsSettingChanged(isChecked);
    }

    private void onShowActorStyleCheckChanged(boolean isChecked) {
        presenter.onShowActorStyleSettingChanged(isChecked);
    }

    private void onSimplifyTagsCheckChanged(boolean isChecked) {
        presenter.onSimplifyTagsSettingChanged(isChecked);
    }


    // ------------------------------------------------------------------------------ VIEW INTERFACE

    @Override
    public void showSubtitleLines(@NonNull List<SubtitleLineVso> subtitleLineVsos) {
        subtitleLinesAdapter.setItems(subtitleLineVsos);
        updateCenterText();
    }

    @Override
    public void showSubtitleLines(@NonNull List<SubtitleLineVso> subtitleLineVsos,
                                  @NonNull DiffUtil.DiffResult diffResult) {
        subtitleLinesAdapter.setItemsDontNotify(subtitleLineVsos);
        diffResult.dispatchUpdatesTo(subtitleLinesAdapter);
        updateCenterText();
    }

    @Override
    public void removeAllCurrentSubtitleData() {
	    subtitleLinesAdapter.clear();
	    searchView.setText("");
    }

    @Override
    public void showTranslatorScreen(long lineId) {
        Intent translatorIntent = new Intent(this, TranslatorActivity.class);
        translatorIntent.putExtra(TranslatorActivity.INPUT_LINE_ID, lineId);
        startActivityForResult(translatorIntent, REQUEST_CODE_TRANSLATOR_ACTIVITY);
    }

    @Override
    public void updateSubtitleLines(@NonNull List<SubtitleLineVso> subtitleLineVsos) {
        for(SubtitleLineVso vso : subtitleLineVsos) subtitleLinesAdapter.updateItem(vso);
    }

    @Override
    public void updateSubtitleLines() {
        subtitleLinesAdapter.notifyDataSetChanged();
    }

    @Override
    public void showProgressLoadingFile() {
        progressLabel.setText(R.string.common_loading_file);
        FadeAnimationHelper.fadeView(true, progressBar, false);
    }

    @Override
    public void showCurrentQuickSettings(boolean showTimings, boolean showActorStyle, boolean simplifyTags) {
        if(showTimingsDrawerItem.isChecked() ^ showTimings)
            drawer.updateItem(showTimingsDrawerItem.withChecked(showTimings));
        if(showActorStyleDrawerItem.isChecked() ^ showActorStyle)
            drawer.updateItem(showActorStyleDrawerItem.withChecked(showActorStyle));
        if(simplifyTagsDrawerItem.isChecked() ^ simplifyTags)
            drawer.updateItem(simplifyTagsDrawerItem.withChecked(simplifyTags));
    }

    @Override
    public void endSearch() {
	    searchView.setText("");
        searchViewContainer.setVisibility(View.GONE);
        searchViewShadow.setVisibility(View.GONE);
    }

    @Override
    public void showSearchQuery(@NonNull String currentSearchQuery) {
        searchViewContainer.setVisibility(View.VISIBLE);
        searchViewShadow.setVisibility(View.VISIBLE);
        searchView.setText(currentSearchQuery);
    }

    @Override
    public void hideProgress() {
	    FadeAnimationHelper.fadeView(false, progressBar, false);
    }

    @Override
    public void showErrorLoadingSubtitleInvalidFormat(@NonNull String filename) {
        Toast.makeText(this, getString(R.string.editor_error_writing_invalid_format, filename),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void showErrorLoadingFailed(@NonNull String filename) {
        Toast.makeText(this, getString(R.string.common_error_writing_failed, filename),
                Toast.LENGTH_LONG).show();
    }


    // ------------------------------------------------------------------------------------ INTERNAL

    private void updateCenterText() {
        if(subtitleLinesAdapter.getItemCount() == 0 && centerTextView.getVisibility() == View.GONE) {
            centerTextView.setText(R.string.editor_subtitle_list_no_lines_in_file);
            FadeAnimationHelper.fadeView(true, centerTextView, false);
        }
        else if(subtitleLinesAdapter.getItemCount() != 0 && centerTextView.getVisibility() == View.VISIBLE)
            FadeAnimationHelper.fadeView(false, centerTextView, false);
    }

    private void showFileOpenSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_OPEN_SUBTITLE);
    }

}
