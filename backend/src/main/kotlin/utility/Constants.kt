package utility


const val SinglelineCommentToken = "//"

val MultilineCommentRegex = Regex("/\\*[\\s\\S]*?\\*/", RegexOption.MULTILINE)
