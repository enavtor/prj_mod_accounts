package com.droidmare.accounts.views.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmare.accounts.R;
import com.droidmare.accounts.models.Multimedia;
import com.droidmare.accounts.utils.FileUtils;
import com.droidmare.accounts.views.activities.FilesActivity;

import java.util.ArrayList;

//Folders view adapter declaration
//@author Eduardo on 13/06/2019.
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private static ArrayList<Multimedia> fileList;

    private final static int THUMB_SIZE = 200;

    private int fileToFocus;
    private int focusedViewPosition;

    private FilesActivity filesActivity;

    public FilesAdapter(ArrayList<Multimedia> items, FilesActivity activity) {
        fileList = items;
        filesActivity = activity;
        fileToFocus = filesActivity.getElementToFocus();
        focusedViewPosition = -1;
    }

    @Override
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;

        switch (viewType) {
            case FileUtils.FOLDER_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_folder, parent, false);
                break;
            case FileUtils.IMAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_image, parent, false);
                break;
        }

        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final FileViewHolder holder, int position) {

        final Multimedia item = fileList.get(position);

        final boolean itemIsFolder = item.getType() == FileUtils.FOLDER_TYPE;

        if (itemIsFolder) {
            if (item.getIcon() != null) holder.image.setImageBitmap(BitmapFactory.decodeFile(item.getIcon().getPath()));
            else holder.image.setImageDrawable(filesActivity.getDrawable(R.drawable.icon_default));
        }

        else {
            if (item.getBitmapIcon() == null) new ThumbnailTask(position, holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);

            else {
                holder.image.setImageBitmap(item.getBitmapIcon());
                if (holder.play != null) holder.play.setAlpha(1f);
            }
        }

        holder.name.setText(item.getName());

        holder.setBehaviour(item, itemIsFolder);

        if (position == fileToFocus) {
            holder.itemView.requestFocus();
            fileToFocus = -1;
        }
    }

    public int getFocusedPosition() { return focusedViewPosition; }

    @Override
    public int getItemCount() { return fileList.size(); }

    public class FileViewHolder extends RecyclerView.ViewHolder {

        public TextView name;

        ImageView image, play;
        CardView cardView;
        RelativeLayout focus;

        FileViewHolder(View itemView) {

            super(itemView);

            image = itemView.findViewById(R.id.thumbnail);
            cardView = itemView.findViewById(R.id.cvBg);
            focus = itemView.findViewById(R.id.focus);
            name = itemView.findViewById(R.id.nameFile);
        }

        void setBehaviour (final Multimedia item, final boolean itemIsFolder) {
            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {

                    if(hasFocus){
                        focus.setBackgroundResource(R.drawable.focus_item);
                        cardView.setAlpha(1);
                        name.setTextColor(filesActivity.getResources().getColor(R.color.colorWhite));
                        focusedViewPosition = getAdapterPosition();
                        filesActivity.changeFileDescriptionText(name.getText().toString());
                    }

                    else{
                        if (itemIsFolder) focus.setBackgroundResource(R.color.cardColorText);
                        else focus.setBackgroundResource(R.color.transparent);
                        cardView.setAlpha(0.5f);
                        name.setTextColor(filesActivity.getResources().getColor(R.color.black));
                        focusedViewPosition = -1;
                        filesActivity.changeFileDescriptionText("");
                    }
                }
            });

            if (itemIsFolder) itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String itemName = item.getName();
                    String itemPath = item.getPath();
                    int position =getAdapterPosition();

                    filesActivity.openFolder(itemName, itemPath, position);
                }
            });

            else itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    filesActivity.sendPickedAvatar(item);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return fileList.get(position).getType();
    }

    private static class ThumbnailTask extends AsyncTask<Void,Void,Bitmap> {

        private static final String TAG = ThumbnailTask.class.getCanonicalName();

        private int mPosition;
        private FileViewHolder mHolder;

        ThumbnailTask(int position, FileViewHolder holder) {
            mPosition = position;
            mHolder = holder;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            Bitmap thumb = null;

            try {
                Multimedia multimedia = fileList.get(mPosition);

                if (multimedia.getType() == FileUtils.IMAGE_TYPE) {
                    Bitmap test = getScaledBitmap(fileList.get(mPosition).getPath());
                    thumb = ThumbnailUtils.extractThumbnail(test, THUMB_SIZE, THUMB_SIZE);
                }

            } catch (RuntimeException rtException) {
                Log.e(TAG, "doInBackground. RuntimeException: " + rtException.getMessage());
            }

            return thumb;
        }

        private Bitmap getScaledBitmap(String filePath) {

            BitmapFactory.Options options = new BitmapFactory.Options();

            int sampleSize = options.outWidth > THUMB_SIZE ? options.outWidth / THUMB_SIZE + 1 : 1;

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            return BitmapFactory.decodeFile(filePath, options);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (mPosition < fileList.size()) {

                fileList.get(mPosition).setBitmapIcon(bitmap);

                mHolder.image.setImageBitmap(bitmap);

                if (mHolder.play != null) mHolder.play.setAlpha(1f);
            }
        }
    }
}
