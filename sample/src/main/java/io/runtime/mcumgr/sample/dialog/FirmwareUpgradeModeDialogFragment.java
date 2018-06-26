/*
 * Copyright (c) 2018, Nordic Semiconductor
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.runtime.mcumgr.sample.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import io.runtime.mcumgr.dfu.FirmwareUpgradeManager;
import io.runtime.mcumgr.sample.R;
import io.runtime.mcumgr.sample.fragment.mcumgr.ImageUpgradeFragment;

public class FirmwareUpgradeModeDialogFragment extends DialogFragment {
	private static final String SIS_ITEM = "item";

	private int mSelectedItem;

	public static DialogFragment getInstance() {
		return new FirmwareUpgradeModeDialogFragment();
	}

	@SuppressWarnings("ConstantConditions")
	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mSelectedItem = savedInstanceState.getInt(SIS_ITEM);
		} else {
			mSelectedItem = 0;
		}

		return new AlertDialog.Builder(requireContext())
				.setTitle(R.string.image_upgrade_mode)
				.setSingleChoiceItems(R.array.image_upgrade_options, mSelectedItem,
						(dialog, which) -> mSelectedItem = which)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(R.string.image_upgrade_action_start, (dialog, which) -> {
					final ImageUpgradeFragment parent = (ImageUpgradeFragment) getParentFragment();
					parent.start(getMode());
				})
				.create();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SIS_ITEM, mSelectedItem);
	}

	private FirmwareUpgradeManager.Mode getMode() {
		switch (mSelectedItem) {
			case 2:
				return FirmwareUpgradeManager.Mode.CONFIRM_ONLY;
			case 1:
				return FirmwareUpgradeManager.Mode.TEST_ONLY;
			case 0:
			default:
				return FirmwareUpgradeManager.Mode.TEST_AND_CONFIRM;
		}
	}
}
