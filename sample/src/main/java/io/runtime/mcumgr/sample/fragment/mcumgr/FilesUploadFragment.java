/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.runtime.mcumgr.sample.fragment.mcumgr;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.runtime.mcumgr.sample.R;
import io.runtime.mcumgr.sample.di.Injectable;
import io.runtime.mcumgr.sample.dialog.GenerateFileDialogFragment;
import io.runtime.mcumgr.sample.utils.FsUtils;
import io.runtime.mcumgr.sample.viewmodel.mcumgr.FilesUploadViewModel;
import io.runtime.mcumgr.sample.viewmodel.mcumgr.McuMgrViewModelFactory;

public class FilesUploadFragment extends FileBrowserFragment implements Injectable {

	@Inject
	McuMgrViewModelFactory mViewModelFactory;
	@Inject
	FsUtils mFsUtils;

	@BindView(R.id.file_name)
	TextView mFileName;
	@BindView(R.id.file_path)
	TextView mFileDestination;
	@BindView(R.id.file_size)
	TextView mFileSize;
	@BindView(R.id.status)
	TextView mStatus;
	@BindView(R.id.progress)
	ProgressBar mProgress;
	@BindView(R.id.action_generate)
	Button mGenerateFileAction;
	@BindView(R.id.action_select_file)
	Button mSelectFileAction;
	@BindView(R.id.action_upload)
	Button mUploadAction;
	@BindView(R.id.action_cancel)
	Button mCancelAction;
	@BindView(R.id.action_pause_resume)
	Button mPauseResumeAction;

	private FilesUploadViewModel mViewModel;

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mViewModel = ViewModelProviders.of(this, mViewModelFactory)
				.get(FilesUploadViewModel.class);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull final LayoutInflater inflater,
							 @Nullable final ViewGroup container,
							 @Nullable final Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_card_files_upload, container, false);
	}

	@Override
	public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);
	}

	@SuppressWarnings("ConstantConditions")
	@Override
	public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mFsUtils.getPartition().observe(this, partition -> {
			if (isFileLoaded()) {
				final String fileName = mFileName.getText().toString();
				mFileDestination.setText(getString(R.string.files_file_path, partition, fileName));
			}
		});
		mViewModel.getState().observe(this, state -> {
			switch (state) {
				case UPLOADING:
					mGenerateFileAction.setVisibility(View.GONE);
					mSelectFileAction.setVisibility(View.GONE);
					mUploadAction.setVisibility(View.GONE);
					mCancelAction.setVisibility(View.VISIBLE);
					mPauseResumeAction.setVisibility(View.VISIBLE);
					mPauseResumeAction.setText(R.string.files_action_pause);
					mStatus.setText(R.string.files_upload_status_uploading);
					break;
				case PAUSED:
					mPauseResumeAction.setText(R.string.files_action_resume);
					break;
				case COMPLETE:
					clearFileContent();
					mGenerateFileAction.setVisibility(View.VISIBLE);
					mSelectFileAction.setVisibility(View.VISIBLE);
					mUploadAction.setVisibility(View.VISIBLE);
					mUploadAction.setEnabled(false);
					mCancelAction.setVisibility(View.GONE);
					mPauseResumeAction.setVisibility(View.GONE);
					mStatus.setText(R.string.files_upload_status_completed);
					break;
			}
		});
		mViewModel.getProgress().observe(this, progress -> mProgress.setProgress(progress));
		mViewModel.getError().observe(this, error -> {
			mGenerateFileAction.setVisibility(View.VISIBLE);
			mSelectFileAction.setVisibility(View.VISIBLE);
			mUploadAction.setVisibility(View.VISIBLE);
			mCancelAction.setVisibility(View.GONE);
			mPauseResumeAction.setVisibility(View.GONE);
			printError(error);
		});
		mViewModel.getCancelledEvent().observe(this, nothing -> {
			clearFileContent();
			mFileName.setText(null);
			mFileDestination.setText(null);
			mFileSize.setText(null);
			mStatus.setText(null);
			mGenerateFileAction.setVisibility(View.VISIBLE);
			mSelectFileAction.setVisibility(View.VISIBLE);
			mUploadAction.setVisibility(View.VISIBLE);
			mUploadAction.setEnabled(false);
			mCancelAction.setVisibility(View.GONE);
			mPauseResumeAction.setVisibility(View.GONE);
		});
		mViewModel.getBusyState().observe(this, busy -> {
			mGenerateFileAction.setEnabled(!busy);
			mSelectFileAction.setEnabled(!busy);
			mUploadAction.setEnabled(isFileLoaded() && !busy);
		});

		// Configure GENERATE FILE action
		mGenerateFileAction.setOnClickListener(v -> {
			final DialogFragment dialog = GenerateFileDialogFragment.getInstance();
			dialog.show(getChildFragmentManager(), null);
		});

		// Configure SELECT FILE action
		mSelectFileAction.setOnClickListener(v -> selectFile("*/*"));

		// Restore UPLOAD action state after rotation
		mUploadAction.setEnabled(isFileLoaded());
		mUploadAction.setOnClickListener(v -> {
			final String filePath = mFileDestination.getText().toString();
			mViewModel.upload(filePath, getFileContent());
		});

		// Cancel and Pause/Resume buttons
		mCancelAction.setOnClickListener(v -> mViewModel.cancel());
		mPauseResumeAction.setOnClickListener(v -> {
			if (mViewModel.getState().getValue() == FilesUploadViewModel.State.UPLOADING) {
				mViewModel.pause();
			} else {
				mViewModel.resume();
			}
		});
	}

	public void onGenerateFileRequested(final int fileSize) {
		onFileSelected("Lorem_" + fileSize + ".txt", fileSize);
		setFileContent(FsUtils.generateLoremIpsum(fileSize));
	}

	@Override
	protected void onFileCleared() {
		mUploadAction.setEnabled(false);
	}

	@Override
	protected void onFileSelected(@NonNull final String fileName, final int fileSize) {
		final String partition = mFsUtils.getPartitionString();
		mFileName.setText(fileName);
		mFileDestination.setText(getString(R.string.files_file_path, partition, fileName));
		mFileSize.setText(getString(R.string.files_upload_size_value, fileSize));
	}

	@Override
	protected void onFileLoaded(@NonNull final byte[] data) {
		mUploadAction.setEnabled(true);
		mStatus.setText(R.string.files_upload_status_ready);
	}

	@Override
	protected void onFileLoadingFailed(final int error) {
		mStatus.setText(error);
	}

	private void printError(@NonNull final String error) {
		final SpannableString spannable = new SpannableString(error);
		spannable.setSpan(new ForegroundColorSpan(
						ContextCompat.getColor(requireContext(), R.color.error)),
				0, error.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new StyleSpan(Typeface.BOLD),
				0, error.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		mStatus.setText(spannable);
	}
}
