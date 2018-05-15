package knez.assdroid.subtitle.handler.ass;

import android.support.annotation.NonNull;

import knez.assdroid.subtitle.handler.TagPrettifier;

public class AssTagsPrettifier implements TagPrettifier {

	@NonNull private final String tagReplacement;

    public AssTagsPrettifier(@NonNull String tagReplacement) {
        this.tagReplacement = tagReplacement;
    }

    @SuppressWarnings("UnnecessaryContinue")
    @Override @NonNull
	public String prettifyTags(@NonNull String source) {
        StringBuilder sb = new StringBuilder();
        int tagStartIndex = -1;
        int textStartIndex = 0;
        for(int i = 0; i < source.length(); i++) {
            if(source.charAt(i) == '{') {
                if(tagStartIndex != -1)
                    continue; // this is basically an error case, double {{
                else {
                    sb.append(source.substring(textStartIndex, i));
                    tagStartIndex = i;
                    continue;
                }
            } else if(source.charAt(i) == '}') {
                if(tagStartIndex == -1)
                    continue; // an error - we got a } without an accompanying {
                else {
                    sb.append(tagReplacement);
                    textStartIndex = i+1;
                    tagStartIndex = -1;
                    continue;
                }
            }
        }
        if(textStartIndex < source.length())
            sb.append(source.substring(textStartIndex, source.length()));

        return sb.toString();
	}

}
