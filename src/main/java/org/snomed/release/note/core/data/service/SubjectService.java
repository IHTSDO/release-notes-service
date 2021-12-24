package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectService {

	@Autowired
	private SubjectRepository subjectRepository;

	public Subject create(Subject subject) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(subject.getTitle())) {
			throw new BadRequestException("title is required");
		}
		if (Strings.isNullOrEmpty(subject.getPath())) {
			throw new BadRequestException("path is required");
		}

		subject.setCreatedDate(LocalDate.now());
		return subjectRepository.save(subject);
	}

	public Subject update(final String id, final Subject subjectDetails) {
		Subject subject = find(id);

		// TODO: can we update path if we already have line items for this subject?
		if (subjectDetails.getTitle() != null) {
			subject.setTitle(subjectDetails.getTitle());
		}
		if (subjectDetails.getPath() != null) {
			subject.setPath(subjectDetails.getPath());
		}
		subject.setLastModifiedDate(LocalDate.now());

		return subjectRepository.save(subject);
	}

	public Subject find(final String id) {
		return subjectRepository.findById(id).orElseThrow(() ->	new ResourceNotFoundException("No subject found for id " + id));
	}

	public List<Subject> findByTitle(final String title) {
		return subjectRepository.findByTitle(title);
	}

	public List<Subject> findAll() {
		List<Subject> result = new ArrayList<>();
		Iterable<Subject> foundSubjects = subjectRepository.findAll();
		foundSubjects.forEach(result::add);
		return result;
	}

	public void delete(final String id) {
		if (!subjectRepository.existsById(id)) {
			throw new ResourceNotFoundException("No subject found for id " + id);
		}
		subjectRepository.deleteById(id);
	}

	public void deleteAll() {
		subjectRepository.deleteAll();
	}

}
