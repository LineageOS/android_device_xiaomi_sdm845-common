/*
 * Copyright (C) 2018 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.dirac;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.lineageos.settings.R;

public class DiracSettingsFragment extends PreferenceFragment implements
        OnPreferenceChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String PREF_HEADSET = "dirac_headset_pref";
    private static final String PREF_PRESET = "dirac_preset_pref";

    private TextView mTextView;

    private ListPreference mHeadsetType;
    private ListPreference mPreset;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.dirac_settings);
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        boolean enhancerEnabled = DiracUtils.isDiracEnabled(getActivity());

        mHeadsetType = (ListPreference) findPreference(PREF_HEADSET);
        mHeadsetType.setOnPreferenceChangeListener(this);
        mHeadsetType.setEnabled(enhancerEnabled);
        String[] headsetEntries = new String[] {
            getString(R.string.dirac_headset_default),
            getString(R.string.dirac_headset_earbuds),
            getString(R.string.dirac_headset_in_ear_2013),
            getString(R.string.dirac_headset_piston_1),
            getString(R.string.dirac_headset_general),
            getString(R.string.dirac_headset_general_inear),
            getString(R.string.dirac_headset_piston_basic),
            getString(R.string.dirac_headset_piston_2),
            getString(R.string.dirac_headset_piston_standard),
            getString(R.string.dirac_headset_headphone),
            getString(R.string.dirac_headset_piston_youth),
            getString(R.string.dirac_headset_piston_color),
            getString(R.string.dirac_headset_in_ear),
            getString(R.string.dirac_headset_capsule),
            getString(R.string.dirac_headset_in_ear_pro),
            getString(R.string.dirac_headset_comfort),
            getString(R.string.dirac_headset_reduction_noise),
            getString(R.string.dirac_headset_cancelling),
            getString(R.string.dirac_headset_half_in_ear),
            getString(R.string.dirac_headset_in_ear2),
            getString(R.string.dirac_headset_earphone_basic),
            getString(R.string.dirac_headset_earphone)
        };
        String[] headsetValues = new String[] {
            "0", "1", "2", "3", "5", "6", "7", "8", "9", "10",
                "11", "12", "13", "14", "15", "16", "17", "18",
                    "19", "20", "21", "22", "23"
        };
        mHeadsetType.setEntries(headsetEntries);
        mHeadsetType.setEntryValues(headsetValues);

        mPreset = (ListPreference) findPreference(PREF_PRESET);
        mPreset.setOnPreferenceChangeListener(this);
        mPreset.setEnabled(enhancerEnabled);
        String[] presetEntries = new String[] {
            getString(R.string.dirac_preset_default),
            getString(R.string.dirac_preset_rock),
            getString(R.string.dirac_preset_jazz),
            getString(R.string.dirac_preset_pop),
            getString(R.string.dirac_preset_classical),
            getString(R.string.dirac_preset_hiphop),
            getString(R.string.dirac_preset_blues),
            getString(R.string.dirac_preset_electronic),
            getString(R.string.dirac_preset_country),
            getString(R.string.dirac_preset_dance),
            getString(R.string.dirac_preset_metal),
        };
        String[] presetValues = new String[] {
            "0,0,0,0,0,0,0",
            "4,2,-2,0,-2,-2,4",
            "0,0,0,-2,-3,0,0",
            "0,-3,-5,0,0,-3,0",
            "0,0,0,0,3,6,6",
            "3,3,-3,0,-3,0,2",
            "2,4,-6,4,0,1,2",
            "3,3,-1,0,-3,0,0",
            "0,0,-2,-2,2,2,0",
            "0,4,2,0,-2,-2,4",
            "2,0,0,-2,-4,0,0"
        };
        mPreset.setEntries(presetEntries);
        mPreset.setEntryValues(presetValues);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.dirac,
                container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean enhancerEnabled = DiracUtils.isDiracEnabled(getActivity());

        mTextView = view.findViewById(R.id.switch_text);
        mTextView.setText(getString(enhancerEnabled ?
                R.string.switch_bar_on : R.string.switch_bar_off));

        View switchBar = view.findViewById(R.id.switch_bar);
        Switch switchWidget = switchBar.findViewById(android.R.id.switch_widget);
        switchWidget.setChecked(enhancerEnabled);
        switchWidget.setOnCheckedChangeListener(this);
        switchBar.setActivated(enhancerEnabled);
        switchBar.setOnClickListener(v -> {
            switchWidget.setChecked(!switchWidget.isChecked());
            switchBar.setActivated(switchWidget.isChecked());
        });
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PREF_HEADSET:
                DiracUtils.setHeadsetType(Integer.parseInt(newValue.toString()));
                return true;
            case PREF_PRESET:
                DiracUtils.setLevel(String.valueOf(newValue));
                return true;
            default: return false;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        DiracUtils.setMusic(isChecked);

        mTextView.setText(getString(isChecked ? R.string.switch_bar_on : R.string.switch_bar_off));

        mHeadsetType.setEnabled(isChecked);
        mPreset.setEnabled(isChecked);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }
}
