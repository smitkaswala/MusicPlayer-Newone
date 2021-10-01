package com.musiclover.newmusic.ui.page.librarypage.playlist;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ldt.musicr.R;
import com.musiclover.newmusic.ui.MusicServiceActivity;
import com.musiclover.newmusic.ui.page.MusicServiceFragment;
import com.musiclover.newmusic.ui.page.subpages.singleplaylist.SinglePlaylistFragment;
import com.musiclover.newmusic.loader.medialoader.PlaylistLoader;
import com.musiclover.newmusic.model.Playlist;
import com.musiclover.newmusic.ui.page.featurepage.FeaturePlaylistAdapter;
import com.musiclover.newmusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaylistChildTab extends MusicServiceFragment implements FeaturePlaylistAdapter.PlaylistClickListener {
    public static final String TAG = "PlaylistChildTab";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    private final PlaylistChildAdapter mAdapter = new PlaylistChildAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen_tab_playlist_list, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setShowAuto(true);
        mAdapter.setOnItemClickListener(this);
        if (getActivity() instanceof MusicServiceActivity) {
            ((MusicServiceActivity) getActivity()).addMusicServiceEventListener(this);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        ViewCompat.setOnApplyWindowInsetsListener(mRecyclerView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                int _8dp = (int) v.getResources().getDimension(R.dimen._8dp);
                v.setPadding(_8dp + insets.getSystemWindowInsetLeft(),
                        _8dp,
                        _8dp + insets.getSystemWindowInsetRight(),
                        _8dp + (int) (insets.getSystemWindowInsetBottom() + v.getResources().getDimension(R.dimen.bottom_back_stack_spacing)));
                return ViewCompat.onApplyWindowInsets(v, insets);
            }
        });
        refreshData();
    }

    private void refreshData() {
        if (getActivity() != null) {
            mAdapter.setData(PlaylistLoader.getAllPlaylistsWithAuto(getActivity()));
        }
    }

    @Override
    public void onClickPlaylist(Playlist playlist, @org.jetbrains.annotations.Nullable Bitmap bitmap) {
        NavigationFragment sf = SinglePlaylistFragment.newInstance(playlist, bitmap);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof NavigationFragment)
            ((NavigationFragment) parentFragment).getNavigationController().presentFragment(sf);
    }

    @Override
    public void onMediaStoreChanged() {
        refreshData();
    }

}
