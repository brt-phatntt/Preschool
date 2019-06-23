package com.example.preschool;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.preschool.Chats.MessageActivity;
import com.example.preschool.TimeLine.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Comment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import de.hdodenhof.circleimageview.CircleImageView;

public class NewsFeedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private DatabaseReference UsersRef, PostsRef, LikesRef;
    private RecyclerView postList;
    private String currentUserID;
    private FirebaseAuth mAuth;
    Boolean LikeChecker = false;

    private FloatingActionButton addPost;
    private static String idClass, idTeacher;
    private Bundle bundle;

    private FirebaseRecyclerAdapter adapter;
    private Boolean isTeacher=false;

    @SuppressLint("RestrictedApi")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_news_feed, container, false);

        // Get Bundle
        bundle = getArguments();
        if (bundle != null) {
            idClass = bundle.getString("ID_CLASS");
            idTeacher=bundle.getString("ID_TEACHER");
        }

        addPost = view.findViewById(R.id.floating_add_post);
        addPost.setVisibility(View.INVISIBLE);
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPostActivity();
            }
        });

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        //

        //nếu là giáo viên mới cho phép đăng bài
        if(currentUserID.equals(idTeacher)){
            addPost.show();
            isTeacher=true;
        }
        else addPost.hide();


        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        postList = view.findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Class").child(idClass).child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Class").child(idClass).child("Likes");
//        DisplayAllUsersPosts();


        return view;

    }


    //hiển thị bảng tin
    private void DisplayAllUsersPosts() {
        Query SortPostsInDecendingOrder = PostsRef;
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(SortPostsInDecendingOrder, Posts.class).build();
        adapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {


            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, int position, @NonNull Posts posts) {

                final String PostKey = getRef(position).getKey();
                UsersRef.child(idTeacher).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String image=dataSnapshot.child("profileimage").getValue().toString();
                            final String name=dataSnapshot.child("fullname").getValue().toString();
                            postsViewHolder.setFullname(name);
                            postsViewHolder.setProfileImage(image);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                if(isTeacher){
                    postsViewHolder.optionButton.setVisibility(View.VISIBLE);
                }

                postsViewHolder.setDescription(posts.getDescription());
                postsViewHolder.setPostImage(posts.getPostimage());

                Calendar calFordTime = Calendar.getInstance();
                int hours = calFordTime.get(Calendar.HOUR_OF_DAY);
                int minutes = calFordTime.get(Calendar.MINUTE);
                int seconds = calFordTime.get(Calendar.SECOND);

                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                String saveCurrentTime = currentTime.format(calFordTime.getTime());

                postsViewHolder.setMinute(posts.getTime());
//                postsViewHolder.setDate(posts.getDate());
//                postsViewHolder.SetTime(posts.getTime());

                postsViewHolder.setLikeButtonStatus(PostKey);
                postsViewHolder.setCommentPostButtonStatus(PostKey);

                //click post activity chua lm

                // click option button
                postsViewHolder.optionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[] = new CharSequence[]{
                                "Edit this post",
                                "Delete this post"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Select Option");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
//                                                Intent profileintent=new Intent(getActivity(),PersonProfileActivity.class);
//                                                profileintent.putExtra("visit_user_id",usersIDs);
//                                                profileintent.putExtra("idTeacher",idTeacher);
//                                                profileintent.putExtra("idClass",idClass);
//                                                startActivity(profileintent);
                                }
                                if (which == 1) {
                                    PostsRef.child(PostKey).removeValue();
//
                                }
                            }
                        });
                        builder.show();
                    }
                });

                //cmt
                postsViewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentsIntent = new Intent(getActivity(), CommentsActivity.class);
                        bundle.putString("KEY_POST",PostKey);
                        commentsIntent.putExtras(bundle);
                        startActivity(commentsIntent);
                    }
                });
                //like
                postsViewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker = true;
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (LikeChecker.equals(true)) {
                                    if (dataSnapshot.child(PostKey).hasChild(currentUserID)) {
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    } else {
                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });

            }


            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
                View view;
                // create a new view

                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.post_layout, parent, false);
                return new PostsViewHolder(view, viewType);

            }


        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        postList.setAdapter(adapter);

    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
//        adapter.startListening();
        DisplayAllUsersPosts();
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {

        private TextView LikePostButton, CommentPostButton;
        private TextView DisplayNoOfLikes, DisplayNoOfComments;
        private ImageButton optionButton;
        private int countLikes;
        private int countComments;

        private String currentUserId;
        private DatabaseReference LikesRef;
        private DatabaseReference CommentsRef;
        public PostsViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            LikePostButton = itemView.findViewById(R.id.like_button);
            CommentPostButton = itemView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);
            DisplayNoOfComments = itemView.findViewById(R.id.display_no_of_comments);
            optionButton=itemView.findViewById(R.id.post_option_button);
            optionButton.setVisibility(View.GONE);

            /**
             * quăng id class vô chổ này classtest1
             *
             */
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Class").child(idClass).child("Likes");
            /////////////////////////////////////////////
            CommentsRef = FirebaseDatabase.getInstance().getReference().child("Class").child(idClass).child("Posts");
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();


        }

        public void setCommentPostButtonStatus(final String PostKey) {
            CommentsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).child("Comments").hasChild(currentUserId)) {
                        countComments = (int) dataSnapshot.child(PostKey).child("Comments").getChildrenCount();
                        DisplayNoOfComments.setText((Integer.toString(countComments) + " Comments"));
                    } else {
                        countComments = (int) dataSnapshot.child(PostKey).child("Comments").getChildrenCount();
                        DisplayNoOfComments.setText((Integer.toString(countComments) + " Comments"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        //set like button status
        public void setLikeButtonStatus(final String PostKey) {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)) {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_black_25dp, 0, 0, 0);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes) + " Likes"));
                        LikePostButton.setTextColor(Color.parseColor("#FF5722"));
                    } else {
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_25dp, 0, 0, 0);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes) + " Likes"));
                        LikePostButton.setTextColor(Color.parseColor("#959292"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


        public void setFullname(String fullname) {
            TextView username = (TextView) itemView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }

        public void setProfileImage(String profileimage) {
            CircleImageView image = (CircleImageView) itemView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).resize(200, 0).into(image);
        }

        public void setMinute(String minute) {
            TextView PostMinute = itemView.findViewById(R.id.post_minute);
            PostMinute.setText(minute + " min ago");
        }

        public void setDescription(String description) {
            TextView postDescription = (TextView) itemView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }

        public void setPostImage(String postImage) {
            ImageView postImages = (ImageView) itemView.findViewById(R.id.post_image);
            Picasso.get().load(postImage).resize(600, 0).into(postImages);
        }

    }

    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(getActivity(), PostActivity.class);
        //////////////////////////////////////////
        addNewPostIntent.putExtras(bundle);
        startActivity(addNewPostIntent);
    }


    @Override
    public void onRefresh() {

    }
}
