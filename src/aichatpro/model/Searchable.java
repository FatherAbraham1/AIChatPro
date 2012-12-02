package aichatpro.model;

/**
 *
 * @author gmochid
 */
public interface Searchable {
    /**
     * Mengeset pattern yang akan menjadi acuan untuk search
     * @param pattern pattern yang akan dijadikan acuan
     */
    public void setup(String pattern);
    
    /*
     * mencari text s dari pattern yang telah diberikan
     */
    public int search(String s);
}
