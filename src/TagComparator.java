import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class TagComparator implements Comparator<Tag>
{
    public int compare(Tag t0, Tag t1)
    {
        if(t0.prob > t1.prob)
            return -1;
        else if(t0.prob == t1.prob)
            return 0;
        else
            return 1;
    }

    public static void main(String[] args)
    {
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
        Tag[] a = (Tag[])arr.toArray();
        Arrays.sort(a, new TagComparator());
        for(Tag t: arr)
            System.out.print(t.prob + " ");
        System.out.println("");
    }
}