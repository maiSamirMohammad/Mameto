package com.example.mameto

data class CommentData (var mUserName: String,
                        var mUserPhoto: Int,
                        var mCommentContent:String,
                        var mCommentsTimeAgo: String){


        private var isShrrink: Boolean = true

        public fun isShrrink(): Boolean {
            return isShrrink
    }

}
