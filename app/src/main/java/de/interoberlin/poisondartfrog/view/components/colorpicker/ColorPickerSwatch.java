/*
 * Copyright (C) 2013 The Android Open Source Project
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

package de.interoberlin.poisondartfrog.view.components.colorpicker;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import de.interoberlin.poisondartfrog.R;

/**
 * Creates a circular swatch of a specified color.  Adds a checkmark if marked as checked.
 */
public class ColorPickerSwatch extends FrameLayout implements View.OnClickListener {
    private int color;
    private ImageView swatchImage;
    private ImageView checkmarkImage;

    private OnColorSelectedListener ocListener;

    // ----------------
    // Constructors
    // ----------------

    public ColorPickerSwatch(Context context) {
        super(context);
    }

    public ColorPickerSwatch(Context context, int color, boolean checked,
                             OnColorSelectedListener listener) {
        super(context);
        this.color = color;
        ocListener = listener;

        LayoutInflater.from(context).inflate(R.layout.color_picker_swatch, this);
        swatchImage = (ImageView) findViewById(R.id.color_picker_swatch);
        checkmarkImage = (ImageView) findViewById(R.id.color_picker_checkmark);
        setColor(color);
        setChecked(checked);
        setOnClickListener(this);
    }

    // ----------------
    // Methods
    // ----------------

    protected void setColor(int color) {
        Drawable[] colorDrawable = new Drawable[]
                {ContextCompat.getDrawable(getContext(), R.drawable.color_picker_swatch)};
        swatchImage.setImageDrawable(new ColorStateDrawable(colorDrawable, color));
    }

    private void setChecked(boolean checked) {
        if (checked) {
            checkmarkImage.setVisibility(View.VISIBLE);
        } else {
            checkmarkImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (ocListener != null) {
            ocListener.onColorSelected(color);
        }
    }

    // ----------------
    // Callback interfaces
    // ----------------

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
}
