/*
 * Copyright (c) 2018, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.sample.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import io.runtime.mcumgr.sample.R;
import io.runtime.mcumgr.sample.fragment.mcumgr.FilesUploadFragment;

public class GenerateFileDialogFragment extends DialogFragment {
	private InputMethodManager mImm;

	public static DialogFragment getInstance() {
		return new GenerateFileDialogFragment();
	}

	@Override
	public void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@SuppressWarnings("ConstantConditions")
	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final LayoutInflater inflater = requireActivity().getLayoutInflater();
		final View view = inflater.inflate(R.layout.dialog_generate_file, null);
		final EditText fileSize = view.findViewById(R.id.file_size);

		final AlertDialog dialog = new AlertDialog.Builder(requireContext())
				.setTitle(R.string.files_upload_generate_title)
				.setView(view)
				// Setting the positive button listener here would cause the dialog to dismiss.
				// We have to validate the value before.
				.setPositiveButton(R.string.files_action_generate, null)
				.setNegativeButton(android.R.string.cancel, null)
				.create();
		dialog.setOnShowListener(d -> mImm.showSoftInput(fileSize, InputMethodManager.SHOW_IMPLICIT));
		dialog.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
			try {
				final int size = Integer.parseInt(fileSize.getText().toString());

				final FilesUploadFragment parent = (FilesUploadFragment) getParentFragment();
				parent.onGenerateFileRequested(size);
				dismiss();
			} catch (final NumberFormatException e) {
				fileSize.setError(getString(R.string.files_upload_generate_error));
			}
		});
		return dialog;
	}
}
