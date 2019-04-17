package com.iaasimov.data.repo;

import com.iaasimov.data.model.Booking;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface BookingRepo extends PagingAndSortingRepository<Booking, Long> {
    public Booking findByQaIdAndUserId(long qaId, String userId);
    public Booking findFirstByUserId(String userId);
}
