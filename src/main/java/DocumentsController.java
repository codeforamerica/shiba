import org.codeforamerica.shiba.UploadDocumentConfiguration;
import org.codeforamerica.shiba.application.Application;
import org.codeforamerica.shiba.application.ApplicationRepository;
import org.codeforamerica.shiba.documents.DocumentRepositoryService;
import org.codeforamerica.shiba.output.MnitDocumentConsumer;
import org.codeforamerica.shiba.pages.config.ApplicationConfiguration;
import org.codeforamerica.shiba.pages.config.FeatureFlagConfiguration;
import org.codeforamerica.shiba.pages.config.LandmarkPagesConfiguration;
import org.codeforamerica.shiba.pages.data.ApplicationData;
import org.codeforamerica.shiba.pages.data.UploadedDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.UUID;

@Controller
public class DocumentsController {
    private final int MAX_FILES_UPLOADED;
    private final ApplicationData applicationData;
    private final FeatureFlagConfiguration featureFlags;
    private final ApplicationRepository applicationRepository;
    private final MnitDocumentConsumer mnitDocumentConsumer;
    private final ApplicationConfiguration applicationConfiguration;
    private final UploadDocumentConfiguration uploadDocumentConfiguration;
    private final DocumentRepositoryService documentRepositoryService;

    public DocumentsController(
            ApplicationConfiguration applicationConfiguration,
            ApplicationData applicationData,
            ApplicationRepository applicationRepository,
            FeatureFlagConfiguration featureFlags,
            UploadDocumentConfiguration uploadDocumentConfiguration,
            DocumentRepositoryService documentRepositoryService,
            MnitDocumentConsumer mnitDocumentConsumer,
            @Value("${document-upload.max-files-uploaded}") int maxFilesUploaded
) {
        this.applicationData = applicationData;
        this.applicationConfiguration = applicationConfiguration;
        this.applicationRepository = applicationRepository;
        this.featureFlags = featureFlags;
        this.uploadDocumentConfiguration = uploadDocumentConfiguration;
        this.documentRepositoryService = documentRepositoryService;
        this.mnitDocumentConsumer = mnitDocumentConsumer;
        this.MAX_FILES_UPLOADED = maxFilesUploaded;
    }

    @PostMapping("/submit-documents")
    ModelAndView submitDocuments() {
        if (featureFlags.get("submit-via-api").isOn()) {
            Application application = applicationRepository.find(applicationData.getId());
            application.getApplicationData().setUploadedDocs(applicationData.getUploadedDocs());
            applicationRepository.save(application);
            mnitDocumentConsumer.processUploadedDocuments(application);
        }
        LandmarkPagesConfiguration landmarkPagesConfiguration = applicationConfiguration.getLandmarkPages();
        String terminalPage = landmarkPagesConfiguration.getTerminalPage();

        return new ModelAndView(String.format("redirect:/pages/%s", terminalPage));
    }

    @PostMapping("/file-upload")
    @ResponseStatus(HttpStatus.OK)
    public void upload(@RequestParam("file") MultipartFile file,
                       @RequestParam("dataURL") String dataURL,
                       @RequestParam("type") String type) throws IOException, InterruptedException {
        if (this.applicationData.getUploadedDocs().size() <= MAX_FILES_UPLOADED &&
                file.getSize() <= uploadDocumentConfiguration.getMaxFilesizeInBytes()) {
            String s3FilePath = String.format("%s/%s", applicationData.getId(), UUID.randomUUID());
            documentRepositoryService.upload(s3FilePath, file);
            this.applicationData.addUploadedDoc(file, s3FilePath, dataURL, type);
        }
    }

    @SuppressWarnings("SpringMVCViewInspection")
    @PostMapping("/remove-upload/{filename}")
    ModelAndView removeUpload(@PathVariable String filename) {
        applicationData.getUploadedDocs().stream()
                .filter(uploadedDocument -> uploadedDocument.getFilename().equals(filename))
                .map(UploadedDocument::getS3Filepath)
                .findFirst()
                .ifPresent(documentRepositoryService::delete);
        this.applicationData.removeUploadedDoc(filename);

        return new ModelAndView("redirect:/pages/uploadDocuments");
    }
}
