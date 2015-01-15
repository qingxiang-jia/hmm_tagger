import java.util.ArrayList;

/**
 * Association of a word and all of its possible tags
 */
public class EntryLine
{
    String word;
    ArrayList<Tag> tags;
    public EntryLine(String word)
    {
        this.word = word;
        tags = new ArrayList<Tag>();
        tags.add(new Tag("I-PER"));
        tags.add(new Tag("I-ORG"));
        tags.add(new Tag("I-LOC"));
        tags.add(new Tag("I-MISC"));
        tags.add(new Tag("B-PER"));
        tags.add(new Tag("B-ORG"));
        tags.add(new Tag("B-LOC"));
        tags.add(new Tag("B-MISC"));
        tags.add(new Tag("O"));
    }
}