import java.util.ArrayList;

/**
 * Find the tag with maximum tag.prob in an array of tags.
 */
public class MaxTag
{
    /**
     * Return the tag with maximum tag.prob in arr.
     * @param arr An array of tags
     * @return Return the tag with maximum tag.prob in arr.
     */
    public Tag getMax(ArrayList<Tag> arr)
    {
        double max = -Double.MAX_VALUE;
        Tag maxTag = null;
        for(Tag t: arr)
            if(t.prob != Double.NaN && t.prob > max)
            {
                maxTag = t;
                max = maxTag.prob;
            }
        return maxTag;
    }

    /** Test during development **/
    public static void main(String[] args)
    {
        MaxTag max = new MaxTag();
        ArrayList<Tag> arr = new ArrayList<Tag>();
        Tag A = new Tag("A");
        Tag B = new Tag("B");
        Tag C = new Tag("C");
        Tag D = new Tag("D");
        Tag E = new Tag("E");
        A.prob = 1.2;
        B.prob = Double.NaN;
        C.prob = 4.2;
        D.prob = Double.NaN;
        E.prob = 2.0;
        arr.add(A);
        arr.add(B);
        arr.add(C);
        arr.add(D);
        arr.add(E);
        System.out.println(max.getMax(arr).prob);
    }
}
