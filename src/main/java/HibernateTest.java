import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateTest {
    public static void main(String[] args) {
        try (SessionFactory factory = new Configuration().configure().buildSessionFactory();
             Session session = factory.openSession()) {
            System.out.println("✅ Hibernate connected successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}