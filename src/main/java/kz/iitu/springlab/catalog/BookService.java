package kz.iitu.springlab.catalog;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class BookService {
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        List<Book> books = repository.findAll();
        if (author == null || author.isBlank()) return books;
        String query = author.toLowerCase(Locale.ROOT);
        return books.stream()
                .filter(book -> book.author().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book create(Book book) {
        validate(book);
        return repository.save(new Book(null, book.title(), book.author(), book.year()));
    }

    public Optional<Book> replace(long id, Book book) {
        validate(book);
        return repository.replace(id, book);
    }

    /** Individual variant #3: only properties present in PATCH body are changed. */
    public Optional<Book> patch(long id, Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            throw badRequest("PATCH body must contain at least one field");
        }
        if (!Set.of("title", "author", "year").containsAll(fields.keySet())) {
            throw badRequest("Only title, author and year may be changed");
        }
        return repository.findById(id).map(existing -> {
            String title = fields.containsKey("title")
                    ? checkedString(fields.get("title"), "title") : existing.title();
            String author = fields.containsKey("author")
                    ? checkedString(fields.get("author"), "author") : existing.author();
            int year = fields.containsKey("year")
                    ? checkedYear(fields.get("year")) : existing.year();
            Book updated = new Book(id, title, author, year);
            validate(updated);
            return repository.replace(id, updated).orElseThrow();
        });
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }

    private static String checkedString(Object value, String field) {
        if (!(value instanceof String s) || s.isBlank()) {
            throw badRequest(field + " must be a nonblank string");
        }
        return s;
    }

    private static int checkedYear(Object value) {
        if (!(value instanceof Number number) ||
                number.doubleValue() != Math.rint(number.doubleValue())) {
            throw badRequest("year must be an integer");
        }
        return number.intValue();
    }

    private static void validate(Book book) {
        if (book == null || book.title() == null || book.title().isBlank() ||
                book.author() == null || book.author().isBlank() ||
                book.year() < 1450 || book.year() > 2100) {
            throw badRequest("Valid title, author and year (1450-2100) are required");
        }
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
