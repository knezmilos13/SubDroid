package knez.assdroid.editor;

import butterknife.BindView;
import butterknife.ButterKnife;
import knez.assdroid.common.mvp.CommonSubtitleActivity;
import knez.assdroid.common.mvp.CommonSubtitleMvp;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.SwitchDrawerItem;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import static knez.assdroid.editor.EditorMvp.*;

public class EditorActivity extends CommonSubtitleActivity
        implements ViewInterface, SubtitleLineLayoutItem.Callback {

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

    @Inject protected Provider<PresenterInterface> presenterProvider;
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
            presenter = presenterProvider.get();
        }

        presenter.onAttach(this);
	}

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return presenter;
    }

    @Override
    protected CommonSubtitleMvp.PresenterInterface getPresenter() {
        return presenter;
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.setListener(searchViewListener);
        hasInitiallySetSearchListener = true;
    }

    @Override
    protected void onPause() {
        searchView.setListener(null);
        hasInitiallySetSearchListener = false;
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
        menu.getItem(0).setIcon(new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_search)
                .color(Color.WHITE)
                .sizeDp(20));
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
                searchView.focusToInputField();
                break;
			case R.id.menu_item_create_subtitle:
			    presenter.onNewSubtitleRequested();
				break;
            case R.id.menu_item_load_subtitle:
                showFileOpenSelector();
                break;
            case R.id.menu_item_about:
                presenter.onAboutScreenRequested();
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

    // Helps with listener complicating things when it gets fired because of automatic restore
    // instance state. Needed when manually removing/adding listener to avoid firing when
    // programmatically changing text
    boolean hasInitiallySetSearchListener = false;
    @NonNull BgpSearchView.Listener searchViewListener = new BgpSearchView.Listener() {
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
    };

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
    public void removeAllSubtitleLines() {
	    subtitleLinesAdapter.clear();
        updateCenterText();
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
    public void closeSearchSection() {
        setSearchText("");
        searchViewContainer.setVisibility(View.GONE);
        searchViewShadow.setVisibility(View.GONE);
    }

    @Override
    public void showSearchSectionWithQuery(@NonNull String currentSearchQuery) {
        searchViewContainer.setVisibility(View.VISIBLE);
        searchViewShadow.setVisibility(View.VISIBLE);
        setSearchText(currentSearchQuery);
        // TODO nabaci fokus odmah na search view
    }

    @Override
    public void showSearchNumbers(int total, int currentIndex) {
	    searchView.setNumResults(currentIndex, total);
    }

    @Override
    public void hideSearchNumbers() {
        searchView.hideNumResults();
    }

    @Override
    public void updateSubtitleLine(@NonNull SubtitleLineVso subtitleLineVso) {
        subtitleLinesAdapter.updateItem(subtitleLineVso);
    }

    @Override
    public void scrollToLine(@NonNull SubtitleLineVso subtitleLineVso) {
        int position = subtitleLinesAdapter.getItemPositionForId((int) subtitleLineVso.getId());
        if(position == -1) return;
        itemListRecycler.scrollToPosition(position);
    }

    @Override
    public int getFirstShownLineNumber() {
        int firstVisiblePosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
        if(firstVisiblePosition == RecyclerView.NO_POSITION) return 0;

        return ((SubtitleLineVso) subtitleLinesAdapter.getItem(firstVisiblePosition)).getLineNumber();
    }

    @Override
    public void showAboutScreen() {
        new LibsBuilder()
                .withAboutAppName(getString(R.string.app_name))
                .withAboutIconShown(true)
                .withAboutVersionShown(true)
                .withAboutDescription(getString(R.string.app_description))
                .withActivityTitle(getString(R.string.common_about_application))
                .withAboutSpecial1(getString(R.string.common_privacy_policy))
                .withAboutSpecial1Description(getString(R.string.privacy_policy_text))
                .withActivityTheme(R.style.SubDroid_Theme_Light)
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .start(this);
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

    private void setSearchText(@NonNull String text) {
        searchView.setListener(null);
        searchView.setText(text);
        if(hasInitiallySetSearchListener) searchView.setListener(searchViewListener);
    }

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
